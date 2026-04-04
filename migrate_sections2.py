import json
import re

with open(r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main\java\com\example\myapplication\viewmodel\EnhancedCityActivity.java", 'r', encoding='utf-8') as f:
    java_content = f.read()

section_pattern = re.compile(r'addSection\(container,\s*"([^"]+)",\s*"((?:[^"\\]|\\.)*)",\s*(true|false)\)', re.DOTALL)

# Mapare directa: json_id -> keyword_in_java
city_keyword_map = {
    "brasov":           "Brasov",
    "campullung":       "Campulung-Moldovenesc",
    "clujnapoca":       "Cluj",
    "constanta":        "Constanta",
    "gurahumorului":    "Gura-Humorului",
    "sibiu":            "Sibiu",
    "targujiu":         "TarguJiu",
    "vatradornei":      "Vatra-Dornei",
    # deja migrate - adaugam si pe astea sa fie complet
    "arad":             "Arad",
    "bacau":            "Bacau",
    "baiamare":         "BaiaMare",
    "baileherculane":   "BaileHerculane",
    "borsa":            "Borsa",
    "bucuresti":        "Bucuresti",
    "buzau":            "Buzau",
    "caransebes":       "Caransebes",
    "cernavoda":        "Cernavoda",
    "craiova":          "Craiova",
    "drobetaturnuseverin": "DrobetaTurnuSeverin",
    "iasi":             "Iasi",
    "lugoj":            "Lugoj",
    "oradea":           "Oradea",
    "pitesti":          "Pitesti",
    "ploiesti":         "Ploiesti",
    "radauti":          "Radauti",
    "resita":           "Resita",
    "sapanta":          "Sapanta",
    "sighetu":          "SighetuMarmatiei",
    "slatina":          "Slatina",
    "suceava":          "Suceava",
    "targoviste":       "Targoviste",
    "timisoara":        "Timisoara",
    "tulcea":           "Tulcea",
    "valcea":           "RamnicuValcea",
    "viseu":            "ViseuDeSus",
}

def extract_sections_for_keyword(keyword):
    # Gasim blocul specific pentru acest keyword
    pattern = rf'className\.contains\("{re.escape(keyword)}"\)[^{{]*{{(.*?)(?=\}} else if|\}}\s*$)'
    match = re.search(pattern, java_content, re.DOTALL)
    if not match:
        return []
    block = match.group(1)
    sections = []
    for m in section_pattern.finditer(block):
        title = m.group(1)
        content = m.group(2).replace('\\n', '\n').replace('\\"', '"').replace('\\\\', '\\')
        highlighted = m.group(3) == 'true'
        sections.append({"title": title, "content": content, "highlighted": highlighted})
    return sections

with open(r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main\assets\cities_data.json", 'r', encoding='utf-8') as f:
    data = json.load(f)

updated = 0
for city in data['cities']:
    city_id = city['id']
    if city_id in city_keyword_map:
        keyword = city_keyword_map[city_id]
        sections = extract_sections_for_keyword(keyword)
        if sections:
            city['sections'] = sections
            city['description'] = sections[0]['content']
            updated += 1
            print(f"✅ {city_id}: {len(sections)} sectiuni")
        else:
            print(f"❌ {city_id}: nu s-au gasit sectiuni pentru keyword '{keyword}'")

with open(r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main\assets\cities_data.json", 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print(f"\n✅ Total actualizate: {updated} orase")
