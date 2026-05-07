import paramiko

c = paramiko.SSHClient()
c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
c.connect('10.42.0.1', username='pi', password='mesh')

script = '''
import struct, json, gzip

f = open("/opt/mesh_pi5_server/maps/offline.pmtiles", "rb")
header = f.read(127)
version = header[7]
print("PMTiles version:", version)

metadata_offset = struct.unpack_from("<Q", header, 24)[0]
metadata_length = struct.unpack_from("<Q", header, 32)[0]
print("Metadata offset:", metadata_offset, "length:", metadata_length)

f.seek(metadata_offset)
raw = f.read(metadata_length)
try:
    data = gzip.decompress(raw)
    meta = json.loads(data)
    if "vector_layers" in meta:
        for vl in meta["vector_layers"]:
            print("LAYER:", vl["id"])
    else:
        for k in list(meta.keys())[:20]:
            print(k, ":", str(meta[k])[:200])
except Exception as e:
    print("Error:", e)
    print("Raw bytes:", raw[:100])
f.close()
'''

stdin, stdout, stderr = c.exec_command(f'python3 -c \'{script}\'')
print(stdout.read().decode())
err = stderr.read().decode()
if err:
    print("STDERR:", err)
c.close()
