#!/usr/bin/env python3
"""
Query weather with two providers:
1) wttr.in (no key)
2) Open-Meteo fallback (geocoding + forecast, no key)

Usage:
    python get-weather.py --city "Shanghai"
"""

from __future__ import annotations

import argparse
import json
import sys
import urllib.parse
import urllib.request
from datetime import datetime, timezone
from typing import Any


USER_AGENT = "weather-query-custom-skill/1.0"


def http_get_json(url: str, timeout: int = 15) -> Any:
    req = urllib.request.Request(url, headers={"User-Agent": USER_AGENT})
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        return json.loads(resp.read().decode("utf-8"))


def weather_code_to_text(code: int) -> str:
    code_map = {
        0: "Clear sky",
        1: "Mainly clear",
        2: "Partly cloudy",
        3: "Overcast",
        45: "Fog",
        48: "Depositing rime fog",
        51: "Light drizzle",
        53: "Moderate drizzle",
        55: "Dense drizzle",
        56: "Light freezing drizzle",
        57: "Dense freezing drizzle",
        61: "Slight rain",
        63: "Moderate rain",
        65: "Heavy rain",
        66: "Light freezing rain",
        67: "Heavy freezing rain",
        71: "Slight snow fall",
        73: "Moderate snow fall",
        75: "Heavy snow fall",
        77: "Snow grains",
        80: "Slight rain showers",
        81: "Moderate rain showers",
        82: "Violent rain showers",
        85: "Slight snow showers",
        86: "Heavy snow showers",
        95: "Thunderstorm",
        96: "Thunderstorm with slight hail",
        99: "Thunderstorm with heavy hail",
    }
    return code_map.get(code, f"Unknown({code})")


def query_wttr(city: str) -> dict[str, Any]:
    safe_city = urllib.parse.quote(city.strip())
    url = f"https://wttr.in/{safe_city}?format=j1"
    payload = http_get_json(url)

    current = payload["current_condition"][0]
    weather = payload["weather"]
    nearest = payload.get("nearest_area", [{}])[0]
    area_name = nearest.get("areaName", [{}])[0].get("value", city)
    country = nearest.get("country", [{}])[0].get("value", "")

    forecast = []
    for day in weather[:3]:
        hourly = day.get("hourly", [])
        summary = hourly[4]["weatherDesc"][0]["value"] if len(hourly) > 4 else ""
        forecast.append(
            {
                "date": day.get("date"),
                "max_c": day.get("maxtempC"),
                "min_c": day.get("mintempC"),
                "uv_index": day.get("uvIndex"),
                "summary": summary,
            }
        )

    return {
        "source": "wttr.in",
        "query_time": datetime.now(timezone.utc).isoformat(timespec="seconds").replace("+00:00", "Z"),
        "location": {
            "city": area_name,
            "country": country,
        },
        "current": {
            "temp_c": current.get("temp_C"),
            "feels_like_c": current.get("FeelsLikeC"),
            "humidity": current.get("humidity"),
            "wind_kmph": current.get("windspeedKmph"),
            "weather_desc": current.get("weatherDesc", [{}])[0].get("value", ""),
        },
        "forecast": forecast,
    }


def query_open_meteo(city: str) -> dict[str, Any]:
    geo_url = (
        "https://geocoding-api.open-meteo.com/v1/search?"
        + urllib.parse.urlencode(
            {
                "name": city.strip(),
                "count": 1,
                "language": "zh",
                "format": "json",
            }
        )
    )
    geo_payload = http_get_json(geo_url)
    results = geo_payload.get("results", [])
    if not results:
        raise ValueError(f"City not found: {city}")

    loc = results[0]
    lat = loc["latitude"]
    lon = loc["longitude"]

    weather_url = (
        "https://api.open-meteo.com/v1/forecast?"
        + urllib.parse.urlencode(
            {
                "latitude": lat,
                "longitude": lon,
                "timezone": "auto",
                "current": "temperature_2m,relative_humidity_2m,apparent_temperature,weather_code,wind_speed_10m",
                "daily": "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max",
            }
        )
    )
    payload = http_get_json(weather_url)

    cur = payload.get("current", {})
    daily = payload.get("daily", {})
    dates = daily.get("time", [])
    max_temp = daily.get("temperature_2m_max", [])
    min_temp = daily.get("temperature_2m_min", [])
    codes = daily.get("weather_code", [])
    rain_prob = daily.get("precipitation_probability_max", [])

    forecast = []
    for i in range(min(3, len(dates))):
        code = int(codes[i]) if i < len(codes) and codes[i] is not None else -1
        forecast.append(
            {
                "date": dates[i],
                "max_c": max_temp[i] if i < len(max_temp) else None,
                "min_c": min_temp[i] if i < len(min_temp) else None,
                "rain_probability_max": rain_prob[i] if i < len(rain_prob) else None,
                "summary": weather_code_to_text(code),
            }
        )

    cur_code = int(cur.get("weather_code", -1)) if cur.get("weather_code") is not None else -1
    return {
        "source": "open-meteo",
        "query_time": datetime.now(timezone.utc).isoformat(timespec="seconds").replace("+00:00", "Z"),
        "location": {
            "city": loc.get("name", city),
            "country": loc.get("country", ""),
            "latitude": lat,
            "longitude": lon,
        },
        "current": {
            "temp_c": cur.get("temperature_2m"),
            "feels_like_c": cur.get("apparent_temperature"),
            "humidity": cur.get("relative_humidity_2m"),
            "wind_kmph": cur.get("wind_speed_10m"),
            "weather_desc": weather_code_to_text(cur_code),
        },
        "forecast": forecast,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description="Query weather by city name.")
    parser.add_argument("--city", required=True, help="City name, e.g. Beijing")
    args = parser.parse_args()

    city = args.city.strip()
    if not city:
        print(json.dumps({"error": "city is empty"}, ensure_ascii=False))
        return 2

    errors = []
    for provider in (query_wttr, query_open_meteo):
        try:
            result = provider(city)
            print(json.dumps(result, ensure_ascii=False))
            return 0
        except Exception as exc:  # noqa: BLE001
            errors.append(str(exc))

    print(
        json.dumps(
            {
                "error": "all weather providers failed",
                "details": errors,
            },
            ensure_ascii=False,
        )
    )
    return 1


if __name__ == "__main__":
    sys.exit(main())
