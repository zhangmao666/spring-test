---
name: weather-query-custom
description: Query current weather and short forecasts by city using free public APIs without API keys. Use when users ask about weather, temperature, humidity, wind, rain probability, or short-term forecast for a city, and you need real weather data instead of guesses.
---

# Weather Query Custom

## Overview

Fetch real-time weather and 3-day forecast from `wttr.in` with automatic fallback to Open-Meteo.
Use the bundled script and answer users in clear natural language with practical tips.

## Workflow

1. Extract a city name from the user message.
2. If city is missing or ambiguous, ask a short clarifying question.
3. Run:
   `python "<skills_dir>/weather-query-custom/scripts/get-weather.py" --city "<CITY>"`
4. Parse returned JSON:
   - `source`
   - `location.city`, `location.country`
   - `current.temp_c`, `current.feels_like_c`, `current.humidity`, `current.wind_kmph`, `current.weather_desc`
   - `forecast` (up to 3 days)
5. Summarize weather in user language and include practical suggestions (clothing, umbrella, outdoor activity).

## Output Guidance

Use this structure when replying:

1. Current conditions:
   - Weather, temperature, feels-like, humidity, wind
2. Next 1-3 days:
   - Daily max/min and short summary
3. Advice:
   - Umbrella/sunscreen/clothing/commute tip

## Error Handling

If script returns:
- `{"error":"all weather providers failed"...}`: explain briefly and ask user to retry or provide another city name.
- `city is empty` or city-not-found detail: ask user for a valid city name (prefer city + country when ambiguous).

## Script

`scripts/get-weather.py`

The script returns normalized JSON from free providers:
- First choice: `wttr.in`
- Fallback: Open-Meteo geocoding + forecast

No API key required.
