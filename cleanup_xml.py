import os
import xml.etree.ElementTree as ET

cities_to_delete = [
    "AlbaIulia", "Arad", "Bacau", "BaiaMare", "BaileHerculane", "Borsa", "Brasov", 
    "Bucuresti", "Buzau", "CampulLung", "Caransebes", "Cernavoda", "ClujNapoca", 
    "Constanta", "Craiova", "Drobetaturnuseverin", "GuraHumorului", "Iasi", "Lugoj", 
    "Oradea", "PiatraNeamt", "Pitesti", "Ploiesti", "Radauti", "Resita", "Sapanta", 
    "Sibiu", "Sighetu", "Slatina", "Suceava", "Targoviste", "TarguJiu", "TarguMures", 
    "Timisoara", "Tulcea", "Valcea", "VatraDornei", "Viseu"
]

manifest_path = r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main\AndroidManifest.xml"

# Register namespaces
ET.register_namespace('android', 'http://schemas.android.com/apk/res/android')
ET.register_namespace('tools', 'http://schemas.android.com/tools')

tree = ET.parse(manifest_path)
root = tree.getroot()
application = root.find('application')

deleted_count = 0
if application is not None:
    activities_to_remove = []
    for activity in application.findall('activity'):
        name = activity.get('{http://schemas.android.com/apk/res/android}name')
        if name:
            for city in cities_to_delete:
                if name == f".RomApp.{city}" or name == f".{city}":
                    activities_to_remove.append(activity)
                    break
    
    for activity in activities_to_remove:
        application.remove(activity)
        deleted_count += 1

tree.write(manifest_path, encoding='utf-8', xml_declaration=True)
print(f"Removed {deleted_count} activities from AndroidManifest.xml safely.")
