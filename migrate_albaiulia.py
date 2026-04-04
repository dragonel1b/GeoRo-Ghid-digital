import json

alba_sections = [
    {"title": "Introducere", "content": "Alba Iulia este unul dintre cele mai importante orașe istorice din România, supranumit 'Capitala Unirii'. Aici, pe 1 Decembrie 1918, s-a proclamat Marea Unire a Transilvaniei cu România, un moment definitoriu în istoria națională.", "highlighted": True},
    {"title": "Geografie", "content": "Situată în centrul Transilvaniei, pe malul stâng al Mureșului, în Depresiunea Alba, orașul este accesibil din toate direcțiile. Clima este temperat-continentală, cu veri călduroase și ierni moderate.", "highlighted": False},
    {"title": "Istorie", "content": "Alba Iulia are o istorie de peste 2000 de ani, începând ca așezare dacică Apulum, devenind apoi cel mai important centru roman din Dacia. În Evul Mediu a fost capitala principatului Transilvaniei și reședința episcopiei catolice. Punctul culminant al istoriei orașului este 1 Decembrie 1918, când s-a proclamat unirea Transilvaniei cu România.", "highlighted": False},
    {"title": "Cetatea Alba Carolina", "content": "Cetatea de tip Vauban, construită de habsburgi între 1716-1733, este cea mai mare și mai bine conservată cetate stelată din România. Cu 7 bastioane și 6 intrări monumentale, cetatea adăpostește catedrala încoronării, palatul apusean și Muzeul Național al Unirii.", "highlighted": False},
    {"title": "Catedrala Încoronării", "content": "Construită în stil neo-românesc (1921-1922), Catedrala Reîntregirii Neamului a găzduit încoronarea regelui Ferdinand I și a reginei Maria în 1922, devenind simbol al Marii Uniri. Este una dintre cele mai frumoase catedrale ortodoxe din România.", "highlighted": False},
    {"title": "Cultură", "content": "Alba Iulia găzduiește Muzeul Național al Unirii (unul dintre cele mai importante muzee de istorie din România), Bibliotecă Batthyaneum (cu manuscrise și incunabule prețioase), Teatrul Municipal. Schimbarea Gărzii în fața cetății este o atracție turistică deosebită.", "highlighted": False},
    {"title": "Gastronomie", "content": "Bucătăria specifică zonei Alba combină preparate tradiționale românești și ardelenești: tochitură cu mămăligă, sarmale, ciorbe, preparate din carne de porc. Vinurile din podgoriile Aiudului și Sebeșului sunt recunoscute pentru calitate.", "highlighted": False},
]

with open(r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main\assets\cities_data.json", 'r', encoding='utf-8') as f:
    data = json.load(f)

for city in data['cities']:
    if city['id'] == 'albaiulia':
        city['sections'] = alba_sections
        city['description'] = alba_sections[0]['content']
        if not city.get('region'):
            city['region'] = 'Transilvania'
        print(f"✅ albaiulia: {len(alba_sections)} sectiuni adaugate")

with open(r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main\assets\cities_data.json", 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print("✅ Done!")
