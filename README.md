# GeoRo - Aplicație Educațională despre România

Aplicație Android pentru învățarea despre geografia, cultura și tradițiile României prin quiz-uri interactive, jocuri și activități culinare.

## Funcționalități Principale

- **Quiz Regional**: Întrebări despre toate regiunile României
- **Jocuri Interactive**: Mini-jocuri tradiționale românești
- **Călătorie Culinară**: Rețete tradiționale pe regiuni
- **Leaderboard Animat**: Clasament cu podium spectaculos și animații
- **Profil Utilizator**: Statistici personale și progres

## 🏆 Leaderboard Spectaculos - NOU!

### Podium Animat cu Tema României
- **Animații de intrare**: Efect staggered pentru top 3 jucători
- **Coroană câștigătorului**: Animație specială pentru primul loc
- **Sparkles efecte**: 5 stele scânteiete în jurul podium-ului
- **Flag overlays**: Steagul României pe fiecare poziție
- **Gradiente dinamice**: Aur, argint, bronz cu design românesc
- **Glow effects**: Efecte continue de strălucire

### Cum să testezi:
1. **Accesează Leaderboard-ul** din meniul principal
2. **Long click pe toolbar** pentru a adăuga date de test
3. **Observă animațiile spectaculoase**: intrarea podium-ului, coroana, sparkles
4. **Testează regiunile**: Schimbă între Transilvania, Muntenia, etc.

## Testarea Profilului Utilizator

### Cum să testezi profilul:

1. **Autentifică-te** în aplicație cu un cont Firebase
2. **Accesează profilul** din meniul principal
3. **Pentru date de test**: Dacă profilul este gol, fă **long click pe toolbar** pentru a adăuga date de test
4. **Verifică funcționalitățile**:
   - Header cu gradient și avatar
   - Badge-uri premium și coroană (pentru utilizatori cu multe puncte)
   - Statistici quick (rang curent, cel mai bun scor)
   - Grid cu statistici detaliate
   - Activitate recentă
   - Editare profil

### Funcționalități de dezvoltare:

- **Long click pe toolbar**: Adaugă date de test în profil (2850 puncte, 47 quiz-uri, 87.5% acuratețe)
- **Creează rezultate de quiz**: Se generează automat 8 rezultate de test cu regiuni și scoruri variate
- **Mesaj informativ**: După 2 secunde, apare un tooltip cu instrucțiuni

### Structura datelor:

- **UserProfile**: Puncte quiz, total quiz-uri, acuratețe, status premium
- **QuizResult**: Rezultate individuale de quiz cu regiunea, tipul jocului, scor, acuratețe, data
- **Statistici calculate**: Rang în regiunea selectată, cel mai bun scor, streak de victorii

## Dezvoltare

Proiectul folosește:
- Android SDK
- Firebase Authentication & Firestore
- Material Design Components
- Glide pentru imagini
- Animații XML cu interpolatori avansați
- CircleImageView pentru profile images

## Fișiere Importante

- `PODIUM_ENHANCEMENTS.md` - Documentație detaliată pentru podium-ul animat
- `/res/anim/` - Fișierele de animații pentru podium
- `/res/drawable/` - Gradiente dinamice și flag overlays

## Licență

Acest proiect este dezvoltat pentru scopuri educaționale. 