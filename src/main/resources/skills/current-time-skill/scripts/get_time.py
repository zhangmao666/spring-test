import datetime
now = datetime.datetime.now()
weekdays = ["星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"]
weekday = weekdays[now.weekday()]

print(f"当前日期：{now.strftime('%Y年%m月%d日')} {weekday}")
print(f"当前时间：{now.strftime('%H:%M:%S')}")
print(f"完整时间戳：{now.strftime('%Y-%m-%d %H:%M:%S')}")
print(f"当前年份：{now.year}")
print(f"当前月份：{now.month}")
print(f"当前日：{now.day}")