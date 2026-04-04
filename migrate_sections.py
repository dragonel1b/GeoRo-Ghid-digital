import json
import re

# Citim EnhancedCityActivity.java
with open(r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main\java\com\example\myapplication\viewmodel\EnhancedCityActivity.java", 'r', encoding='utf-8') as f:
    java_content = f.read()

# Extragem blocurile addSection pentru fiecare oras
# Pattern: addSection(container, "Titlu", "Text lung", true/false)
section_pattern = re.compile(r'addSection\(container,\s*"([^"]+)",\s*"((?:[^"\\]|\\.)*)",\s*(true|false)\)', re.DOTALL)

# Identificam blocurile de cod pentru fiecare oras
city_blocks = {
    "constanta": re.search(r'className\.contains\("Constanta"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "oradea": re.search(r'className\.contains\("Oradea"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "arad": re.search(r'className\.contains\("Arad"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "tulcea": re.search(r'className\.contains\("Tulcea"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "cernavoda": re.search(r'className\.contains\("Cernavoda"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "craiova": re.search(r'className\.contains\("Craiova"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "targu_jiu": re.search(r'className\.contains\("TarguJiu"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "gura_humorului": re.search(r'className\.contains\("Gura-Humorului"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "vatra_dornei": re.search(r'className\.contains\("Vatra-Dornei"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "campulung": re.search(r'className\.contains\("Campulung-Moldovenesc"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "radauti": re.search(r'className\.contains\("Radauti"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "drobetaturnuseverin": re.search(r'className\.contains\("DrobetaTurnuSeverin"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "valcea": re.search(r'className\.contains\("RamnicuValcea"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "slatina": re.search(r'className\.contains\("Slatina"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "suceava": re.search(r'className\.contains\("Suceava"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "bacau": re.search(r'className\.contains\("Bacau"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "iasi": re.search(r'className\.contains\("Iasi"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "timisoara": re.search(r'className\.contains\("Timisoara"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "resita": re.search(r'className\.contains\("Resita"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "lugoj": re.search(r'className\.contains\("Lugoj"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "caransebes": re.search(r'className\.contains\("Caransebes"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "baileherculane": re.search(r'className\.contains\("BaileHerculane"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "bucuresti": re.search(r'className\.contains\("Bucuresti"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "ploiesti": re.search(r'className\.contains\("Ploiesti"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "targoviste": re.search(r'className\.contains\("Targoviste"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "pitesti": re.search(r'className\.contains\("Pitesti"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "buzau": re.search(r'className\.contains\("Buzau"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "baiamare": re.search(r'className\.contains\("BaiaMare"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "sighetu": re.search(r'className\.contains\("SighetuMarmatiei"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "borsa": re.search(r'className\.contains\("Borsa"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "viseu": re.search(r'className\.contains\("ViseuDeSus"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
    "sapanta": re.search(r'className\.contains\("Sapanta"\).*?(?=\} else if|\}\s*/\*|\}\s*$)', java_content, re.DOTALL),
}

def extract_sections(block_text):
    sections = []
    for m in section_pattern.finditer(block_text):
        title = m.group(1)
        content = m.group(2).replace('\\n', '\n').replace('\\"', '"').replace('\\\\', '\\')
        highlighted = m.group(3) == 'true'
        sections.append({"title": title, "content": content, "highlighted": highlighted})
    return sections

city_sections = {}
for city_id, match in city_blocks.items():
    if match:
        sections = extract_sections(match.group(0))
        if sections:
            city_sections[city_id] = sections
            print(f"✅ {city_id}: {len(sections)} sectiuni")
    else:
        print(f"❌ {city_id}: nu s-a gasit blocul")

# Citim cities_data.json
with open(r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main\assets\cities_data.json", 'r', encoding='utf-8') as f:
    data = json.load(f)

# Adaugam sectiunile
updated = 0
for city in data['cities']:
    city_id = city['id'].lower().replace(' ', '').replace('-', '').replace('ș','s').replace('ț','t').replace('ă','a').replace('â','a').replace('î','i')
    if city_id in city_sections:
        city['sections'] = city_sections[city_id]
        # Actualizam si description cu prima sectiune
        if city_sections[city_id]:
            city['description'] = city_sections[city_id][0]['content']
        updated += 1

with open(r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main\assets\cities_data.json", 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print(f"\n✅ Actualizate {updated} orase cu sectiuni detaliate in cities_data.json!")
