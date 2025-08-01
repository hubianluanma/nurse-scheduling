

import json
from datetime import datetime
import re
import os
import argparse

try:
    from ics import Calendar, Event
except ImportError:
    print("The 'ics' library is not installed. Please run: pip install ics")
    exit()

def create_ics_for_nurse(nurse_name, year, data):
    """
    Finds a nurse by name and generates an ICS calendar with all-day events.
    """
    found_nurse = None
    for nurse in data['nurses']:
        cleaned_json_name = re.sub(r'[★*]', '', nurse['name'])
        if nurse_name == cleaned_json_name:
            found_nurse = nurse
            break

    if not found_nurse:
        print(f"错误：找不到护士 “{nurse_name}”。")
        return None, None

    calendar = Calendar()
    for date_str, shift in found_nurse['schedule'].items():
        # Skip rest days
        if "休" in shift:
            continue

        match = re.match(r'(\d+)月(\d+)日', date_str)
        if not match:
            continue

        month, day = int(match.group(1)), int(match.group(2))
        try:
            base_date = datetime(year, month, day).date()
        except ValueError:
            print(f"警告：日期格式错误或年份无效 “{date_str}”。")
            continue

        # Create an all-day event for the shift
        event = Event()
        event.name = shift
        event.begin = base_date
        event.make_all_day()
        calendar.events.add(event)

    safe_filename = f"{nurse_name}_{year}_schedule_allday.ics"
    return calendar, safe_filename


def main():
    """
    Main function to run the script.
    """
    parser = argparse.ArgumentParser(description="为护士排班生成全天事件的 ICS 日历文件（默认年份：2025）。")
    parser.add_argument("nurse_name", nargs='?', default=None, help="护士的姓名。")
    args = parser.parse_args()

    json_file = 'schedule.json'
    if not os.path.exists(json_file):
        print(f"错误：文件 '{json_file}' 不存在。")
        return

    with open(json_file, 'r', encoding='utf-8') as f:
        data = json.load(f)

    nurse_names = [re.sub(r'[★*]', '', nurse['name']) for nurse in data['nurses']]
    
    if not args.nurse_name:
        print("用法: python3 schedule_to_ics.py <护士姓名>")
        print("\n可选的护士列表:")
        for name in sorted(nurse_names):
            print(f"- {name}")
        return

    if args.nurse_name not in nurse_names:
        print(f"错误：护士 “{args.nurse_name}” 不在列表中。")
        return

    year = 2025

    calendar, filename = create_ics_for_nurse(args.nurse_name, year, data)

    if calendar and filename:
        with open(filename, 'w', encoding='utf-8') as f:
            f.writelines(calendar)
        print(f"\n成功！日历文件已保存为: {os.path.abspath(filename)}")


if __name__ == '__main__':
    main()
