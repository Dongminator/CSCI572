import urllib.request
import hashlib

BLOCKSIZE = 65536
hasher = hashlib.sha1()
url = "http://1.bp.blogspot.com/-INGz7409puU/Uz241shV0vI/AAAAAAAAAV8/7YbTLsl6DcY/s1600/N1403P59001C.jpg"
response = urllib.request.urlopen(url)
buf = response.read(BLOCKSIZE)
while len(buf) > 0:
    hasher.update(buf)
    buf = response.read(BLOCKSIZE)
print(hasher.hexdigest())
