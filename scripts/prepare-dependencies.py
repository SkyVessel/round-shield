#!/usr/bin/env python3
"""Prepare pinned, hash-verified compile-only APIs; do not redistribute mods."""
from pathlib import Path
from io import BytesIO
import hashlib
import json
import urllib.request
import zipfile

root = Path(__file__).resolve().parent.parent
specs = json.loads((root / 'scripts/dependencies.json').read_text())
downloads = root / 'libs/downloads'
apis = root / 'libs/compile-only'
downloads.mkdir(parents=True, exist_ok=True)
apis.mkdir(parents=True, exist_ok=True)


def download(name, spec):
    path = downloads / (name + '.jar')
    if not path.exists() or hashlib.sha512(path.read_bytes()).hexdigest() != spec['sha512']:
        request = urllib.request.Request(spec['url'], headers={'User-Agent': 'SkyVessel-RoundShield-build/1.0'})
        with urllib.request.urlopen(request, timeout=60) as response:
            data = response.read()
        if hashlib.sha512(data).hexdigest() != spec['sha512']:
            raise RuntimeError('Checksum mismatch: ' + name)
        path.write_bytes(data)
    print('Verified', name, spec['version'])
    return path.read_bytes()


(apis / 'iris.jar').write_bytes(download('iris', specs['iris']))
with zipfile.ZipFile(BytesIO(download('lambdynamiclights', specs['lambdynamiclights']))) as bundle:
    api = bundle.read('META-INF/jars/lambdynamiclights-api-4.11.0+26.1.2.jar')
    (apis / 'lambdynamiclights-api.jar').write_bytes(api)
with zipfile.ZipFile(BytesIO(api)) as bundle:
    for name in ['core', 'event']:
        filename = 'yumi-commons-' + name + '-2.0.0.jar'
        (apis / filename).write_bytes(bundle.read('META-INF/jars/' + filename))
print('Compile-only APIs ready. Run ./gradlew build with Java 25.')
