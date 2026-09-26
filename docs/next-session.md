# Reluare — 27 septembrie 2026

## Stare confirmată

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
