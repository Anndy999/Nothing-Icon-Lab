# Icon Pack Pipeline

Installed Apps → Scanner → ComponentName → Renderer → ZIP → import_iconlab_export.py → drawable-nodpi + appfilter + drawable.xml → Gradle/aapt2 → resources.arsc → Theme Park

```bash
python3 tools/import_iconlab_export.py /path/NothingIconLab-export.zip
./gradlew :iconpack:assembleRelease
```
