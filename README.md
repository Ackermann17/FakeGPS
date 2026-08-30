# FakeGPS

Aplikasi mock location untuk Android, dikembangkan dari [vatrex/FakeGPS](https://github.com/vatrex/FakeGPS) dengan penyempurnaan dan eksplorasi tambahan.

## ✨ Tentang Proyek Ini

Proyek ini adalah pengembangan lanjutan dari repo asli, dengan fokus mempelajari cara kerja location spoofing di Android melalui dua pendekatan berbeda:

1. **Versi Systemless (Non-Root)**
   Menggunakan Mock Location API resmi Android + Google Maps API. Cocok untuk device tanpa akses root, memanfaatkan fitur developer options bawaan Android.

2. **Versi LSPosed Module (Root)**
   Sedang dikembangkan sebagai modul Xposed/LSPosed untuk device yang sudah di-root, memungkinkan kontrol lokasi yang lebih dalam di level sistem.

## 🎯 Tujuan

Proyek ini dibuat untuk keperluan edukasi — memahami arsitektur location service Android, Mock Location API, dan cara kerja framework Xposed/LSPosed dalam memodifikasi perilaku sistem.

## 🛠️ Tech Stack
- Java
- Android SDK
- Google Maps API
- Mock Location API
- LSPosed Framework (dalam pengembangan)

## 📱 Status Pengembangan
- [x] Versi systemless — berhasil di-build & diuji
- [ ] Versi LSPosed module — dalam progres

## 🧪 Testing
Diuji pada device Poco X5 (8/256GB, rooted).

## 🙏 Credit
Dikembangkan dari basis [vatrex/FakeGPS](https://github.com/vatrex/FakeGPS).

## ⚠️ Disclaimer
Proyek ini dibuat untuk tujuan pembelajaran dan pengembangan skill Android development. Gunakan secara bertanggung jawab dan sesuai dengan kebijakan aplikasi/layanan yang berlaku.
