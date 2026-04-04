import json

d = json.load(open(r'c:\Users\Admin\Desktop\ConcursInfo\app\src\main\assets\cities_data.json', encoding='utf-8'))

print('FARA SECTIUNI:')
for c in d['cities']:
    if not c.get('sections'):
        print(f"  - {c['id']} ({c['name']})")

print('\nCU SECTIUNI:')
for c in d['cities']:
    if c.get('sections'):
        print(f"  + {c['id']} ({c['name']}) - {len(c['sections'])} sectiuni")
