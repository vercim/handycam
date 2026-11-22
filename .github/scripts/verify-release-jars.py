import os
from pathlib import Path
from zipfile import ZipFile


endpoint_file = "handycam-telemetry-endpoint.txt"
expected_endpoint = os.environ.get("ORG_GRADLE_PROJECT_telemetrikaEndpoint", "")
required_entries = {
    endpoint_file,
    "dev/vercim/handycam/HandycamTelemetrySettings.class",
    "dev/vercim/handycam/internal/telemetrika/Telemetry.class",
    "META-INF/LICENSE-Telemetrika",
}
targets = sorted(target for target in Path("versions").iterdir() if target.is_dir())
if not targets:
    raise SystemExit("No Stonecutter targets were found.")

for target in targets:
    artifacts = [
        path for path in (target / "build/libs").glob("*.jar")
        if not path.name.endswith(("-sources.jar", "-dev.jar"))
    ]
    if len(artifacts) != 1:
        raise SystemExit(f"{target.name}: expected one release JAR, found {len(artifacts)}.")
    artifact = artifacts[0]
    with ZipFile(artifact) as jar:
        missing = required_entries.difference(jar.namelist())
        if missing:
            raise SystemExit(f"{artifact}: missing {', '.join(sorted(missing))}.")
        if jar.read(endpoint_file).decode("utf-8").strip() != expected_endpoint:
            raise SystemExit(f"{artifact}: incorrect packaged telemetry endpoint.")

print(f"Verified the telemetry core and endpoint in {len(targets)} release JARs.")
