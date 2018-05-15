import urllib2
import json

KUrl = "https://koinex.in/api/ticker"
BUrl = "https://api.binance.com/api/v3/ticker/price"

req = urllib2.Request(KUrl)
opener = urllib2.build_opener()
f = opener.open(req)
Kjson = json.loads(f.read())

req = urllib2.Request(BUrl)
f = opener.open(req)
Bjson = json.loads(f.read())


KCurrencyList = ["AE", "AION", "BAT", "BCH", "BTC", "EOS", "ETH", "GAS", "GNT", "LTC", "NCASH", "NEO", "OMG", "ONT",
                  "REQ", "TRX", "XLM", "XRB", "XRP", "ZRX"]

BCurrencyList = ["AE", "AION", "BAT", "BCC", "BTC", "EOS", "ETH", "GAS", "GNT", "LTC", "NCASH", "NEO", "OMG", "ONT",
                 "REQ", "TRX", "XLM", "NANO", "XRP", "ZRX"]

n = len(KCurrencyList)

KPrices = n*[0.0]
BPrices = n*[0.0]

KRatios = [[0.0 for x in range(n)] for y in range(n)]
BRatios = [[0.0 for x in range(n)] for y in range(n)]

BjsonRange = len(Bjson)

for x in range(n):
    KPrices[x] = float(Kjson["prices"]["inr"][KCurrencyList[x]])
    for y in range(BjsonRange):
        if Bjson[y]["symbol"] == BCurrencyList[x]+"BTC":
            BPrices[x] = float(Bjson[y]["price"])

BPrices[4] = 1.0
print BPrices
arr=[]
for x in range(n):
    for y in range(x+1,n):
        BRatios[x][y] = BPrices[y]/BPrices[x]
        KRatios[x][y] = KPrices[y]/KPrices[x]
        Ktemp = KRatios[x][y]
        Btemp = BRatios[x][y]
        difference = abs(Ktemp-Btemp)
        val = 100 * difference / min(Ktemp,Btemp)
        #arr.append((val,))
        str1= str(val)+" "+KCurrencyList[y]+" -> "+KCurrencyList[x]+"  " + str(KRatios[x][y])+"  "+str(BRatios[x][y])+"  "
        #print str1
        arr.append(str1)
arr.sort(reverse=True)
n=len(arr)
for i in xrange(n):
    print arr[i]

# print KRatios