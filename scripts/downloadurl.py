import urllib.request
import shutil
import time
import os

start = time.time()
prob_url = 0
suffix = ["jpg","JPG","png","PNG","bmp","BMP","gif","GIF","jpeg","JPEG"]
sufcount = [0,0,0,0,0,0,0,0,0,0]
newpath = "images" 
if not os.path.exists(newpath): os.makedirs(newpath)
inf = open("image_urls",'r')
for line in inf:
    url = line
    try:
        response = urllib.request.urlopen(url)
    except urllib.error.HTTPError as e:
        print("problem with url: "+ url + str(e.code))
        prob_url += 1
        continue
    except urllib.error.URLError as e:
        print("problem with url: "+ url + str(e.reason))
        prob_url += 1
        continue
    token = line.split('.')
    for idx,suf in enumerate(suffix):
        if suf+"\n" == token[-1]:
            sufcount[idx] += 1
            file_name = str(sufcount[idx])+"."+suf
            with open("images/"+file_name, 'wb') as out_file:
                shutil.copyfileobj(response, out_file)
            break
inf.close()
for idx,suf in enumerate(suffix):
    print(suf+": "+str(sufcount[idx]))
print("problematic urls: "+str(prob_url))
end = time.time()
print("time elapsed: %.2f seconds"%(end-start))
