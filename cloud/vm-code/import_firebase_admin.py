import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
from datetime import date, timedelta
import datetime
import time
# Use the application default credentials
cred = credentials.Certificate('roomoccupancy-39040-9924c79eddcb.json')
firebase_admin = firebase_admin.initialize_app(cred)
now = datetime.datetime.now()
db = firestore.client()

while(True):
	now = datetime.datetime.now()
	dateNow = now.strftime("%Y%m%d")
	doc_ref = db.collection('processed').document(dateNow)
	procData = doc_ref.get().to_dict()
	procKeys = doc_ref.get().to_dict()
	# print(procData)
	ins = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
	outs = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
	# print(procData)
	if(procData == None):
		yesterday = date.today() - timedelta(1)
		dateYes = yesterday.strftime('%Y%m%d')
		# print(dateYes)
		doc_ref_1 = db.collection('processed').document(dateYes)
		current = doc_ref_1.get().to_dict().get("current")
		# print(current)
		totalIn = 0
		totalOut = 0
		lifeTimeIn = doc_ref_1.get().to_dict().get("lifetimeIn")
		lifeTimeOut = doc_ref_1.get().to_dict().get("lifetimeOut")
			
	else:
		current = procData.get("current")
		totalIn = procData.get("totalIn")
		totalOut = procData.get("totalOut")
		ins = procData.get("inHours")
		outs = procData.get("outHours")
		lifeTimeIn = procData.get("lifetimeIn")
		lifeTimeOut = procData.get("lifetimeOut")


	# print(current, totalIn, totalOut, InOne, InTwo, InThree, InFour)
	doc_ref = db.collection('Raw Data').document(dateNow)
	rawData = doc_ref.get().to_dict()
	if (rawData != None):
		db.collection(u'Unparsed Data').document(dateNow).set(rawData, merge = True)
		doc_ref = db.collection('processed').document(dateNow)
		db.collection('Raw Data').document(dateNow).delete()
		rawDataKeys = list(rawData)
		# print(rawDataKeys)
		# print(keys[0], doc.to_dict().get(keys[0]))
		for key in rawDataKeys:
			if (rawData.get(key) == "IN"):
				current = current + 1
				lifeTimeIn = lifeTimeIn + 1
				for n in range(0,23):
					if((0+n*10000)<= int(key) < (10000 + n*10000)):
						ins[n] = ins[n] + 1
			elif (rawData.get(key) == "OUT"):
				current = current - 1
				lifeTimeOut = lifeTimeOut + 1
				for n in range(0,23):
					if((0+n*10000)<= int(key) < (10000 + n*10000)):
						outs[n] = outs[n] + 1

		

	testData = {
		u'current': current,
		u'totalIns':(sum(ins)),
		u'totalOuts':(sum(outs)),
		u'inHours':ins,
		u'outHours':outs,
		u'lifetimeIn': lifeTimeIn,
		u'lifetimeOut': lifeTimeOut
	}

	print(testData)
	db.collection(u'processed').document(dateNow).set(testData)
	time.sleep(5);