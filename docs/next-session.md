# Reluare — 27 septembrie 2026

## Actualizare după verificarea vocabularului — 27 septembrie 2026

Această secțiune are prioritate față de lista istorică de mai jos.

- Iconițele TV, corectarea fluxului Premium și politica accesibilă din Despre sunt implementate și comise. Testul real Premium prin Google Play a fost amânat explicit de utilizator pentru o sesiune ulterioară.
- Istoric verificat pe Pixel 5 la font 150%: ultima intrare accesibilă prin derulare, toate cele 8 filtre lizibile, sortare după dată și categorie goală verificate. Fontul a fost restaurat la 100%.
- Protecția vocabularului implementată: `DataBaseHelper2` nu mai șterge baza la nepotrivirea/lipsa preferinței `db2Ver`. SQLiteOpenHelper creează schema doar unde lipsește, păstrând inclusiv bazele vechi cu user_version=0. Schema și versiunea 2 sunt neschimbate.
- Resetarea vocabularului din meniul de revenire la valorile inițiale rămâne explicită, după confirmarea separată; cele două tabele sunt golite într-o tranzacție, fără înlocuirea fișierului bazei.
- Build debug/release, testele unitare și lintRelease au trecut. Cele 7 teste instrumentate de vocabular au trecut pe Pixel 5, pe baze temporare: inițializare, redeschidere, preferințe nepotrivite, versiuni vechi, baza reală din assets, resetare și rollback la eroare. Hash-ul bazei reale din aplicația debug a rămas identic înainte/după teste.
- APK debug actualizat pe Pixel 5. Nu s-au șters datele utilizatorului și nu s-a făcut commit automat.
- Exportul unei secțiuni `.erd` și importul din selectorul Android sunt acum implementate în meniul Vocabular, inclusiv butoane TV (cu mesaj dacă dispozitivul nu are selector de documente). UTF-8, validare integrală înainte de confirmare, import tranzacțional, duplicate globale sărite fără suprascriere. Limite: 10.000 de perechi / 2 milioane de caractere. Formatul vechi păstrează doar cuvintele și explicațiile, nu direcția/datele/metadatele; textele care nu pot fi reprezentate fără pierderi sunt refuzate la export. Importul predefinit existent nu a fost schimbat.
- Verificări export/import: 8 teste unitare de format plus 7 Premium trecute; 9 teste instrumentate de bază (inclusiv round-trip și rollback la import) trecute pe Pixel 5. Build debug/release și lint: 0 erori, 99 avertismente. Lint Fragment result routing este suprimat local, documentat: gazda este ComponentActivity, nu FragmentActivity.
- Test manual Pixel: exportat `Download/City - Oras.erd` (75 de perechi, 1748 bytes), redeschis prin Import până la confirmarea corectă, apoi ANULAT. Fișierul exportat a fost lăsat în Descărcări. Hash-ul bazei reale debug a rămas identic. APK debug actualizat; niciun commit automat.
- Asocierea externă `.erd` este implementată prin `ErdImportActivity`, un punct de intrare separat care nu depinde de inițializarea MainActivity. Primește numai ACTION_VIEW/content, verifică numele și conținutul prin același importator și cere confirmare; nu importă automat. Preferința limbii este aplicată și la pornirea la rece. Fișierele brute file:// și URL-urile web sunt refuzate.
- Filtrele acceptă application/octet-stream, text/plain și application/x-erd pentru compatibilitate cu adrese content opace (numerice). Din această cauză Dicționarul poate apărea și pentru alte fișiere de aceste tipuri; validarea refuză fișierele fără extensia .erd. Nu folosiți „Întotdeauna” la testarea selectorului. Referință: https://developer.android.com/guide/components/intents-filters
- Verificat în Files pe Pixel 5 după force-stop: Dicționar englez–român (Test) apare în „Deschide cu”; „Numai o dată” deschide confirmarea corectă pentru 75 de perechi. Importul a fost anulat și hash-ul bazei a rămas identic. Cele 12 teste instrumentate (9 bază + 3 asociere), 15 teste unitare, build debug/release și lint au trecut (0 erori, 100 avertismente). APK debug instalat. TV/tabletă nu au fost testate efectiv pentru transfer.
- Utilizatorul dorește un commit comun pentru toate modificările Vocabularului. Nu s-a făcut commit automat. Mesaj propus: `Protect vocabulary data and add ERD import, export and file association`.
- Rămân de verificat: Premium prin Play, consimțământ refuz/offline, tabletă/TV reale și Vocabular/Verbe cu text mărit. Nu publicați automat.

## Stare confirmată (istoric)

- Versiunea cerută de utilizator: 9.0, versionCode 94, setate în app/build.gradle.
- Despre/About: data 27 septembrie 2026, versiunea citită automat din pachet, contact emanuelboboiu@gmail.com și site HTTPS.
- Build debug/release și lint au trecut înainte de ultima schimbare strictă a numerelor de versiune; recompilați pentru 9.0 (94).
- UMP integrat în Main, Vocabulary și Verbs; debug folosește reclame de test.
- Pe Pixel 5: reclame de test în cele trei ecrane, formular UMP publicat și afișat, alegere confirmată de utilizator, păstrată după repornire, formular redeschis din meniu. Refuzul, schimbarea alegerilor și scenariile offline nu sunt încă verificate complet.
- Politica bilingvă publicată la https://android.pontes.ro/erd/privacy.html și comparată cu fișierul local: corespunde.
- Fișier local pentru FTP: D:\GeneralStorage\Bacups\android\erd\privacy.html. Originalul este salvat alături ca privacy-before-update-20260926.html.
- Politica menționează IP-urile testelor online, păstrare de maximum 3 luni și ștergere manuală, asumată de utilizator. Nu a fost implementată sau verificată ștergerea pe server.

## Priorități convenite / de discutat

1. Premium: inventarierea beneficiilor existente dincolo de eliminarea reclamelor și discutarea micilor funcții suplimentare. Nu modificați accesul la funcții înainte de decizia utilizatorului; țineți cont de beneficiile deja cumpărate. Verificați achiziția și restaurarea.
2. Banner TV românesc corectat la 27 septembrie: drawable-ro-xhdpi/banner.png este acum copia exactă a bannerului complet românesc de 320x180 din drawable-ro/banner.png. La reverificarea înainte de modificare, fișierul xhdpi avea text englezesc (nu fundal gol, cum fusese raportat inițial). Bannerul englez și iconițele mobile au rămas nemodificate.
3. Iconița TV adaptată la 27 septembrie: mipmap-television-xhdpi/ic_launcher.png reutilizează imaginea existentă de 192x192, fără resampling. Pentru API26+, mipmap-television-anydpi-v26 include iconițe adaptive standard/round cu cartea existentă și inset de 25%. Stratul TV folosește copia nodpi de 192x192. Resursele mobile nu au fost schimbate. Configurațiile TV au fost verificate în APK cu aapt2; afișarea într-un launcher TV real/emulat rămâne de verificat. Nu s-a făcut redesign sau test vizual TV.
4. Link permanent către politica de confidențialitate în aplicație: încă NU implementat. Utilizatorul prefera numai magazinul; i s-a explicat cerința Google Play pentru link/text și în aplicație. Propunere: discret în About, inclusiv Premium/TV. Pentru TV verificați deschiderea conținutului web compatibilă cu cerințele platformei.
5. Actualizarea declarațiilor Data safety în Play Console; pagina publică auditată spunea încă «No data collected / No data shared». UMP nu acoperă automat statisticile proprii. Contul Play Console nu a fost inspectat.
6. Teste rămase: refuz/modificare consimțământ, offline, Premium, tabletă, telecomandă TV. Verificările vizuale Vocabular și Verbe la text mărit au fost explicit amânate; nu le uitați. Istoric rămâne și el pe listă.
7. Înainte de publicare: build 9.0 (94), lint, AAB semnat și testare internă. Nu publicați automat. Publicarea era planificată de utilizator pentru seara de 27 septembrie.

## Mod de lucru

- Pași mici, explicații concise în română. Inspectați git status înainte de editări; nu presupuneți că utilizatorul a făcut commit.
- Nu modificați PHP sau date pe server fără cerere explicită. Utilizatorul transferă politica prin FTP.
- Pixel 5: serial 12231FDD4002CW; pachet test ro.pontes.englishromaniandictionary.debug, separat de aplicația Play.
- Cerințe de referință: https://developer.android.com/docs/quality-guidelines/tv-app-quality și https://support.google.com/googleplay/android-developer/answer/10144311
