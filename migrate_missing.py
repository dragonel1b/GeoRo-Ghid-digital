import json

# Sectiunile pentru orasele care nu au bloc in initializeSpecificContent()
# Extrase manual din getCityDescription() + Brasov.java / Sibiu.java / ClujNapoca.java

manual_sections = {
    "brasov": [
        {"title": "Introducere", "content": "Brașov este un important centru turistic din Transilvania, cunoscut pentru Cetatea Medievală, Biserica Neagră și apropierea de stațiunile montane. Situat la poalele Munților Carpați, oferă un peisaj montan spectaculos.", "highlighted": True},
        {"title": "Geografie", "content": "Brașov este situat în centrul României, în depresiunea cu același nume, înconjurat de munți: Bucegi, Piatra Craiului și Postăvarul. Râul Timișa îl străbate, iar Poiana Brasov, la 10 km altitudine, este una dintre cele mai cunoscute stațiuni montane.", "highlighted": False},
        {"title": "Istorie", "content": "Fondat în 1211 de Cavalerii Teutoni, Brașovul medieval a fost un important centru comercial al Transilvaniei. Cetatea a rezistat atacurilor otomane datorită zidurilor sale puternice. Biserica Neagră, cel mai mare edificiu gothic din România, a fost construită în sec XIV-XV.", "highlighted": False},
        {"title": "Cetatea Medievală", "content": "Cetatea Brașovului este una dintre cele mai bine conservate fortificații medievale din România. Turnul Alb, Turnul Negru, Bastionul Țesătorilor și zidurile de apărare pot fi vizitate și astăzi.", "highlighted": False},
        {"title": "Atracții Turistice", "content": "Piața Sfatului, centrul vechi al orașului, este înconjurată de clădiri medievale colorate. Casa Sfatului, azi muzeu, datează din 1420. Stradela Sforii, una dintre cele mai înguste alei din Europa, Biserica Neagră și telecabina spre Postăvarul sunt atracții principale.", "highlighted": False},
        {"title": "Cultură", "content": "Brașovul are o viață culturală activă, cu Filarmonica Brașov, Teatrul Dramatic 'Sică Alexandrescu', Teatrul de Operetă și Muzical, numeroase galerii de artă și festivaluri. Festivalul Golden Stag (Cerbul de Aur) a fost unul dintre cele mai mari festivaluri muzicale din Europa.", "highlighted": False},
        {"title": "Gastronomie", "content": "Bucătăria specifică zonei combină influențe românești cu cele săsești și maghiare. Preparate populare: tochitură ardelenească, papricaș de pui, varză á la Cluj, sarmale, caltaboș și cârnați afumați. Berea locală (o tradiție săsească) și vinul de Dealu Mare completează oferta gastronomică.", "highlighted": False},
    ],
    "constanta": [
        {"title": "Introducere", "content": "Constanța, cunoscută în antichitate sub numele de Tomis, este cel mai vechi oraș atestat de pe teritoriul României. Situată pe coasta Mării Negre, este un important centru economic, cultural și turistic al țării.", "highlighted": True},
        {"title": "Geografie", "content": "Constanța este situată în sud-estul României, pe coasta Mării Negre. Orașul se întinde pe o suprafață de aproximativ 124 km² și include numeroase lacuri precum Siutghiol și Tăbăcărie. Clima este temperat-continentală cu influențe maritime, cu veri călduroase și ierni blânde.", "highlighted": False},
        {"title": "Istorie", "content": "Fondată în secolul al VI-lea î.Hr. de coloniștii greci din Milet, Constanța a fost cunoscută inițial sub numele de Tomis. A fost un important centru comercial și cultural în perioada romană, iar mai târziu a devenit parte a Imperiului Bizantin. În perioada modernă, orașul a cunoscut o dezvoltare rapidă, devenind principalul port al României.", "highlighted": False},
        {"title": "Atracții Turistice", "content": "Cazinoul, Moscheea Carol I, Portul Tomis și plajele moderne sunt doar câteva dintre atracțiile care fac din Constanța o destinație turistică de top. Muzeul de Istorie Națională și Arheologie, Aquarium, Delfinariu și Piața Ovidiu sunt alte obiective importante.", "highlighted": False},
        {"title": "Cultură", "content": "Un oraș multicultural unde se împletesc influențele române, grecești, turcești și tătare, creând un mozaic cultural unic. Teatrul de Stat, Opera Română, Filarmonica și numeroase festivaluri culturale animă viața orașului.", "highlighted": False},
        {"title": "Gastronomie", "content": "Bucătăria dobrogeneană combină influențe românești, turcești și grecești. Specialități: plăcintă dobrogeană cu brânză, kebap, miel la tavă, pește și fructe de mare proaspete din Marea Neagră, salată de icre și baclavale.", "highlighted": False},
    ],
    "sibiu": [
        {"title": "Introducere", "content": "Sibiu este un oraș medieval din Transilvania cu o arhitectură saxonă deosebită. Cunoscut pentru Piața Mare și Piața Mică, Podul Minciunilor, și muzeele sale de renume. A fost Capitală Culturală Europeană în 2007.", "highlighted": True},
        {"title": "Geografie", "content": "Sibiu este situat în centrul Transilvaniei, pe râul Cibin, la poalele Carpaților Meridionali. Orașul se bucură de un climat temperat-continental cu influențe alpine, cu ierni friguroase și veri răcoroase și plăcute.", "highlighted": False},
        {"title": "Istorie", "content": "Fondat în sec. XII de coloniști sași, Sibiu (Hermannstadt) a fost cel mai important centru commercial și cultural al sașilor transilvăneni. A fost reședință a gubernatorului Transilvaniei și momentan este reședința județului Sibiu.", "highlighted": False},
        {"title": "Centrul Istoric", "content": "Centrul istoric al Sibiului, păstrat aproape intact, include Piața Mare, Piața Mică, Piața Huet, Podul Minciunilor (primul pod de fontă din România), Turnul Sfatul și o serie de magazine vechi și palate baroce.", "highlighted": False},
        {"title": "Atracții Turistice", "content": "Muzeul Brukenthal (unul dintre cele mai vechi muzee din Europa), Catedrala Evanghelică, Catedrala Ortodoxă Sf. Treime, Muzeul în Aer Liber ASTRA (cel mai mare muzeu etnografic din România), Turnul Sfatului, Cimitirul Evanghelic sunt atracțiile principale.", "highlighted": False},
        {"title": "Cultură", "content": "Sibiu are o viață culturală extrem de bogată: Festivalul Internațional de Teatru (FITS - unul dintre cele mai importante din Europa), Opera Română, Filarmonica de Stat, Teatrul de Stat, expoziții permanente și temporare în cele 40+ muzee și galerii.", "highlighted": False},
        {"title": "Gastronomie", "content": "Bucătăria sibieană combină tradiții românești și săsești. Preparate specifice: ciolan afumat cu fasole, tochitura ardelenească, cârnați afumați, slănină, cozonac sibian și prăjituri însiropate. Berea Cibin și horinca de casă completează experiența culinară.", "highlighted": False},
    ],
    "clujnapoca": [
        {"title": "Introducere", "content": "Cluj-Napoca este cel mai important centru cultural, universitar și economic din Transilvania și al doilea oraș ca mărime din România. Găzduiește festivaluri internaționale de renume precum TIFF și Untold și are o viață culturală și de noapte vibrantă.", "highlighted": True},
        {"title": "Geografie", "content": "Cluj-Napoca este situat în nord-vestul Transilvaniei, pe râul Someșul Mic, într-o depresiune înconjurată de dealuri și păduri. Clima este temperat-continentală, cu veri calde și ierni moderate.", "highlighted": False},
        {"title": "Istorie", "content": "Atestat documentar din 1213, Clujul a fost un important centru comercial și cultural medieval al Transilvaniei. A aparținut succesiv regatului maghiar, Imperiului Otoman și celui Habsburg, înainte de a deveni parte a României în 1918 și temporar între 1940-1944 parte a Ungariei.", "highlighted": False},
        {"title": "Viața Universitară", "content": "Cluj-Napoca este cel mai important centru universitar din România, cu peste 100.000 de studenți. Universitatea Babeș-Bolyai (una dintre cele mai mari din țară), Universitatea Tehnică, Universitatea de Medicină și Farmacie și alte instituții de  prestigiu atrag studenți din toată țara.", "highlighted": False},
        {"title": "Atracții Turistice", "content": "Piața Unirii cu statuia lui Matei Corvin, Catedrala Sf. Mihail, Palatul Banffy (Muzeul de Artă), Parcul Central cu casino și lac, Grădina Botanică Alexandru Borza, Teatrul Național, Muzeul Național de Istorie a Transilvaniei sunt principalele obiective.", "highlighted": False},
        {"title": "Cultură și Festivaluri", "content": "Cluj-Napoca este capitala culturală neoficială a României. Festivalul Internațional de Film Transilvania (TIFF), Untold Festival, Electric Castle, Jazz in the Park, Festivalul Medieval și zeci de alte evenimente culturale anuale fac din Cluj un hub cultural european.", "highlighted": False},
        {"title": "Gastronomie", "content": "Bucătăria clujeană reflectă moștenirea multiculturală: varză á la Cluj, papricaș de pui, gulaș ardelenesc. Scena gastronomică modernă din Cluj este una dintre cele mai dinamice din România, cu restaurante de fine dining, cafenele specialty coffee și piețe de producători locali.", "highlighted": False},
    ],
}

with open(r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main\assets\cities_data.json", 'r', encoding='utf-8') as f:
    data = json.load(f)

updated = 0
for city in data['cities']:
    cid = city['id']
    if cid in manual_sections:
        city['sections'] = manual_sections[cid]
        city['description'] = manual_sections[cid][0]['content']
        updated += 1
        print(f"✅ {cid}: {len(manual_sections[cid])} sectiuni adaugate")

with open(r"c:\Users\Admin\Desktop\ConcursInfo\app\src\main\assets\cities_data.json", 'w', encoding='utf-8') as f:
    json.dump(data, f, ensure_ascii=False, indent=2)

print(f"\n✅ Total: {updated} orase completate")
