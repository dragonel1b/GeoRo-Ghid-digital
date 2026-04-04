import os
import re
import json

# Define the paths
java_dir = r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main\java\com\example\myapplication\RomApp"
json_path = r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main\assets\cities_data.json"

# Load current JSON data
with open(json_path, 'r', encoding='utf-8') as f:
    data = json.load(f)

# Helper function to find a city in the JSON by its id or name
def find_city(data, city_id, city_name):
    # Try exact match on id or name
    for city in data['cities']:
        if city['id'].lower() == city_id.lower() or city['name'].lower() == city_name.lower():
            return city
    # Try partial matching on ID
    for city in data['cities']:
        if city_id.lower() in city['id'].lower() or city['id'].lower() in city_id.lower():
            return city
    return None

import glob

for filepath in glob.glob(os.path.join(java_dir, "*.java")):
    filename = os.path.basename(filepath)
    city_name_from_file = filename.replace('.java', '')
    
    # We ignore standard activities
    if city_name_from_file in ["CityDetailActivity", "LoginActivity", "MainActivity", 
                               "PointsManager", "RegionTemplate", "TuristiActivity", 
                               "UserActivity", "CityFeaturesActivity", "RomMapActivity", "CityListActivity", "RomQuizActivity"]:
        continue
    
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Find the corresponding city in JSON
    city = find_city(data, city_name_from_file, city_name_from_file)
    if not city:
        print(f"City not found in JSON: {city_name_from_file}")
        # We can also dynamically create the city here if it's missing, but let's assume it exists
        continue
    
    # 1. Extract map coords
    # Uri gmmIntentUri = Uri.parse("geo:44.3302,23.7949?q=Craiova,Romania");
    map_coords_match = re.search(r'geo:([\d\.\-]+,[\d\.\-]+)', content)
    if map_coords_match:
        city['mapCoords'] = map_coords_match.group(1)
        
    # 2. Extract Weather ID
    # WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?id=" + CRAIOVA_CITY_ID 
    weather_match = re.search(r'\"1234567890abcdef1234567890abcdef(?:\"\s*;\s*private\s+static\s+final\s+String\s+\w+_CITY_ID\s*=\s*\")(\d+)\"', content)
    if not weather_match:
        # alternative search
        weather_match = re.search(r'CITY_ID\s*=\s*"(\d+)"', content)
    if weather_match:
        city['weatherId'] = weather_match.group(1)
        
    # 3. Extract Events
    # String[] events = { "Event 1", "Event 2" };
    events_match = re.search(r'String\[\]\s+events\s*=\s*\{([^\}]*)\}', content, re.DOTALL)
    if events_match:
        events_str = events_match.group(1)
        # Parse individual strings
        events = re.findall(r'"([^"]+)"', events_str)
        if events:
            city['events'] = events
            
    # 4. Extract Tips
    tips_match = re.search(r'String\[\]\s+tips\s*=\s*\{([^\}]*)\}', content, re.DOTALL)
    if tips_match:
        tips_str = tips_match.group(1)
        tips = re.findall(r'"([^"]+)"', tips_str)
        if tips:
            city['tips'] = tips
            
    # 5. Extract Attractions
    # AttractionHelper.addAttraction(..., "Name", R.drawable.image_res, "Prompt")
    attractions = []
    # Using regex to find addAttraction calls
    # AttractionHelper.addAttraction(\s*this,\s*container,\s*"([^"]+)",\s*R.drawable.([^,]+),\s*"([^"]+)"\s*)
    for attr_match in re.finditer(r'AttractionHelper\.addAttraction\s*\(\s*[^,]+,\s*[^,]+,\s*"([^"]+)",\s*R\.drawable\.([^,]+)(?:\s*//[^,\n]*)?,\s*"([^"]+)"\s*\)', content):
        attractions.append({
            "name": attr_match.group(1).strip(),
            "imageRes": attr_match.group(2).strip(),
            "prompt": attr_match.group(3).strip()
        })
    if attractions:
        city['attractions'] = attractions
        
    # 6. Extract Default Images
    # defaultImages.add("oltenia_craiova_1");
    # We find setupImageCarouselWithoutUserImages or getCityImages
    images = re.findall(r'defaultImages\.add\("([^"]+)"\);', content)
    # limit to unique and in order
    if images:
        images_set = []
        for img in images:
            if img not in images_set:
                images_set.append(img)
        city['defaultImages'] = images_set
        
    # 7. Extract Description/Sections
    # addSection(..., "Title", "Content"...
    # We can join descriptions.
    sections = ""
    for sec_match in re.finditer(r'addSection\s*\([^\n,]+,\s*"([^"]+)",\s*"([^"]+)",\s*(?:true|false)\s*\)', content):
        title = sec_match.group(1).strip()
        sec_content = sec_match.group(2).strip()
        if len(sec_content) > 10:  # Valid content
            if title.lower() in ['introducere']:
                sections = sec_content + "\n\n" + sections
            else:
                sections += f"## {title}\n{sec_content}\n\n"
    
    if sections.strip():
        city['description'] = sections.strip()

# Write back to JSON
with open(json_path, 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print("✅ Data extraction completed and cities_data.json updated.")
