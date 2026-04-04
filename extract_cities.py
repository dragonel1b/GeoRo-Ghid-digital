import os
import re
import json

src_dir = r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main\java\com\example\myapplication\RomApp"
output_file = r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main\assets\cities_data.json"

cities = []

print("Starting extraction...")
for filename in os.listdir(src_dir):
    if not filename.endswith(".java"): continue

    # Ignore Region files and other non-cities
    if filename in ["Banat.java", "RegionTemplate.java", "Bucovina.java", "Crisana.java", "Dobrogea.java", "Maramures.java", "Moldova.java", "Muntenia.java", "Oltenia.java", "Transilvania.java", "TuristiActivity.java", "MainActivity.java", "LoginActivity.java", "UserActivity.java", "PointsManager.java"]:
        continue
        
    filepath = os.path.join(src_dir, filename)
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()
        
    if "extends EnhancedCityActivity" not in content:
        continue
        
    city = {"id": filename.replace(".java", "").lower()}
    
    # name
    m = re.search(r'getCityName\(\)\s*\{[^}]*return\s*"([^"]+)";', content)
    city["name"] = m.group(1) if m else filename.replace(".java", "")
    
    # region
    m = re.search(r'getRegionName\(\)\s*\{[^}]*return\s*"([^"]+)";', content)
    city["region"] = m.group(1) if m else ""
    
    # desc
    m = re.search(r'getCityDescription\(\)\s*\{[^}]*return\s*"([^"]+)";', content)
    city["description"] = m.group(1) if m else ""
    
    # map
    m = re.search(r'geo:([0-9.]+),([0-9.]+)', content)
    city["mapCoords"] = f"{m.group(1)},{m.group(2)}" if m else ""
    
    # weather
    m = re.search(r'ARAD_CITY_ID = "([0-9]+)"', content) # Many might have copied ARAD_CITY_ID literally
    if not m:
        m = re.search(r'CITY_ID = "([0-9]+)"', content)
    city["weatherId"] = m.group(1) if m else ""
    
    # default images
    images = re.findall(r'defaultImages\.add\("([^"]+)"\);', content)
    city["defaultImages"] = list(dict.fromkeys(images)) # unique
    
    # events
    m = re.search(r'String\[\] events = \{([^}]+)\};', content)
    events = []
    if m:
        event_strs = re.findall(r'"([^"]+)"', m.group(1))
        events.extend(event_strs)
    city["events"] = set(events) # we'll convert back to list
    
    # tips
    m = re.search(r'String\[\] tips = \{([^}]+)\};', content)
    tips = []
    if m:
        tip_strs = re.findall(r'"([^"]+)"', m.group(1))
        tips.extend(tip_strs)
    city["tips"] = set(tips)

    # attractions: addAttraction(this, container, "Cetatea Aradului", "R.drawable.cetate", ...)
    atts = []
    for am in re.finditer(r'AttractionHelper\.addAttraction\s*\(\s*[^,]+,\s*[^,]+,\s*"([^"]+)",\s*(R\.drawable\.[a-zA-Z0-9_]+)[^,]*,\s*"([^"]+)"\s*\)', content):
        name = am.group(1)
        res = am.group(2)
        prompt = am.group(3)
        atts.append({"name": name, "imageRes": res, "prompt": prompt})
    city["attractions"] = atts

    # Convert sets to lists
    city["events"] = list(city["events"])
    city["tips"] = list(city["tips"])
    
    cities.append(city)

# Also handle AlbaIulia manually as an example since it doesn't extend EnhancedCityActivity
alba = {
    "id": "albaiulia",
    "name": "Alba Iulia",
    "region": "Transilvania",
    "description": "Alba Iulia este un oraș plin de istorie, gazda celei mai mari cetăți în stil Vauban din România, Alba Carolina.",
    "mapCoords": "46.0685,23.5714",
    "weatherId": "686575",
    "defaultImages": ["cetate_alba", "alba_1"],
    "events": ["Ziua Națională a României - 1 Decembrie", "Festivalul Cetăților Dacice"],
    "tips": ["Schimbul de gardă la poarta Cetății este spectaculos.", "Închiriază o bicicletă să te plimbi pe șanțurile cetății."],
    "attractions": [
        {"name": "Cetatea Alba Carolina", "imageRes": "R.drawable.cetatea_alba_carolina", "prompt": "Părerea ta..."},
        {"name": "Catedrala Reîntregirii", "imageRes": "R.drawable.catedrala_reintregirii", "prompt": "Părerea ta..."}
    ]
}
cities.append(alba)

os.makedirs(os.path.dirname(output_file), exist_ok=True)
with open(output_file, "w", encoding="utf-8") as f:
    json.dump({"cities": cities}, f, ensure_ascii=False, indent=2)

print(f"Extracted {len(cities)} cities to {output_file}")
