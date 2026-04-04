import os
import re

cities_to_delete = [
    "AlbaIulia", "Arad", "Bacau", "BaiaMare", "BaileHerculane", "Borsa", "Brasov", 
    "Bucuresti", "Buzau", "CampulLung", "Caransebes", "Cernavoda", "ClujNapoca", 
    "Constanta", "Craiova", "Drobetaturnuseverin", "GuraHumorului", "Iasi", "Lugoj", 
    "Oradea", "PiatraNeamt", "Pitesti", "Ploiesti", "Radauti", "Resita", "Sapanta", 
    "Sibiu", "Sighetu", "Slatina", "Suceava", "Targoviste", "TarguJiu", "TarguMures", 
    "Timisoara", "Tulcea", "Valcea", "VatraDornei", "Viseu"
]

romapp_dir = r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main\java\com\example\myapplication\RomApp"
manifest_path = r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main\AndroidManifest.xml"

deleted_count = 0
for city in cities_to_delete:
    path = os.path.join(romapp_dir, f"{city}.java")
    if os.path.exists(path):
        os.remove(path)
        deleted_count += 1
        print(f"Sterg: {city}.java")

with open(manifest_path, 'r', encoding='utf-8') as f:
    manifest_content = f.read()

for city in cities_to_delete:
    # Remove block <activity> ... </activity>
    pattern = r'<activity[^>]*android:name="\.(RomApp\.)?' + city + r'"[^>]*>(.*?)</activity>'
    manifest_content = re.sub(pattern, '', manifest_content, flags=re.DOTALL)
    
    # Remove self-closing <activity />
    pattern_self_closing = r'<activity[^>]*android:name="\.(RomApp\.)?' + city + r'"[^>]*/>'
    manifest_content = re.sub(pattern_self_closing, '', manifest_content)

# Remove empty blank lines left by removals
manifest_content = re.sub(r'\n\s*\n\s*\n', '\n\n', manifest_content)

with open(manifest_path, 'w', encoding='utf-8') as f:
    f.write(manifest_content)

print(f"\n✅ Am sters {deleted_count} clase de orase si le-am scos din AndroidManifest.xml!")
