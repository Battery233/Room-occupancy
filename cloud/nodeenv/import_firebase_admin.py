import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
import datetime
# Use the application default credentials
cred = credentials.Certificate('roomoccupancy-39040-9924c79eddcb.json')
firebase_admin = firebase_admin.initialize_app(cred)
now = datetime.datetime.now()
db = firestore.client()
doc_ref = db.collection('app started').document('firebaseTest')
doc = doc_ref.get()
dateNow = now.strftime("%Y%m%d")
print(dateNow)
print(dateNow[0:2])
doc_ref = db.collection('app started').document(dateNow)
# totalIn = doc_red.get().to_dict().get("totalIn")
# totalOut = doc_red.get().to_dict().get("totalOut")
totalIn = "100"
totalOut = "40"
current = int(totalIn) - int(totalOut)
print(current)
print(doc.to_dict().get("mahbub"))
class Data(object):
        def __init__(self, current, totalIn, totalOut, date):
                self.current = current
                self.totalIn = totalIn
                self.totalOut = totalOut
                self.date = date
        def to_dict(self):
                dest = {'current': self.current, 'totalIn': self.totalIn, 'totalOut': self.totalOut, 'date' : self.date}
                return dest
testData = Data(current=(str(current)), totalIn = (totalIn), totalOut = (totalOut), date = (dateNow))
# Add a new doc in collection 'cities' with ID 'LA'
out = testData.to_dict()
print(out)
db.collection(u'processed').document(dateNow).set(out)