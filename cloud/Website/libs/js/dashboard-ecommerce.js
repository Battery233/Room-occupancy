 
src="https://www.gstatic.com/firebasejs/5.8.4/firebase-app.js"
src="https://www.gstatic.com/firebasejs/5.8.4/firebase-auth.js"
src="https://www.gstatic.com/firebasejs/5.8.4/firebase-database.js"
src="https://www.gstatic.com/firebasejs/5.8.4/firebase-firestore.js"
src="https://www.gstatic.com/firebasejs/5.8.4/firebase-messaging.js"
src="https://www.gstatic.com/firebasejs/5.8.4/firebase-functions.js"


 const firebaseApp = firebase.initializeApp({
    apiKey: "AIzaSyDKV3x9atqHq6_Nbjxz8KkC_kijK7Clcno",
    authDomain: "roomoccupancy-39040.firebaseapp.com",
    databaseURL: "https://roomoccupancy-39040.firebaseio.com",
    projectId: "roomoccupancy-39040",
    storageBucket: "roomoccupancy-39040.appspot.com",
    messagingSenderId: "803591973096"
});





var db = firebaseApp.firestore();



  var d1 = new Date(); 
  var dateString1 =
    d1.getUTCFullYear() +
    ("0" + (d1.getUTCMonth()+1)).slice(-2) +
    ("0" + d1.getUTCDate()).slice(-2);

  var d2 = d1;
  d2.setDate(d1.getDate() - 1);
  var dateString2 =
    d2.getUTCFullYear() +
    ("0" + (d2.getUTCMonth()+1)).slice(-2) +
    ("0" + d2.getUTCDate()).slice(-2);

  var d3 = d2;
  d3.setDate(d2.getDate() - 1);
  var dateString3 =
    d3.getUTCFullYear() +
    ("0" + (d3.getUTCMonth()+1)).slice(-2) +
    ("0" + d3.getUTCDate()).slice(-2);

  var d4 = d3;
  d4.setDate(d3.getDate() - 1);
    var dateString4 =
    d4.getUTCFullYear() +
    ("0" + (d4.getUTCMonth()+1)).slice(-2) +
    ("0" + d4.getUTCDate()).slice(-2);

  var d5 = d4;
  d5.setDate(d4.getDate() - 1);
  var dateString5 =
    d5.getUTCFullYear() +
    ("0" + (d5.getUTCMonth()+1)).slice(-2) +
    ("0" + d5.getUTCDate()).slice(-2);

  var d6 = d5;
  d6.setDate(d5.getDate() - 1);
  var dateString6 =
    d6.getUTCFullYear() +
    ("0" + (d6.getUTCMonth()+1)).slice(-2) +
    ("0" + d6.getUTCDate()).slice(-2);

  var d7 = d6;
  d7.setDate(d6.getDate() - 1);
  var dateString7 =
    d7.getUTCFullYear() +
    ("0" + (d7.getUTCMonth()+1)).slice(-2) +
    ("0" + d7.getUTCDate()).slice(-2);



    const firestore = firebase.firestore();
    const settings = {/* your settings... */ timestampsInSnapshots: true};
    firestore.settings(settings);
    var docRef = db.collection("processed").doc(dateString1);
    docRef.get().then(function(doc) {

    console.log(dateString2);

    var docRef1 = db.collection("processed").doc(dateString2);
        docRef1.get().then(function(doc1) {      

    var docRef2 = db.collection("processed").doc(dateString3);
        docRef2.get().then(function(doc2) {

    var docRef3 = db.collection("processed").doc(dateString4);
        docRef3.get().then(function(doc3) {

    var docRef4 = db.collection("processed").doc(dateString5);
        docRef4.get().then(function(doc4) {

    var docRef5 = db.collection("processed").doc(dateString6);
        docRef5.get().then(function(doc5) {

    var docRef6 = db.collection("processed").doc(dateString7);
        docRef6.get().then(function(doc6) {

    console.log(doc1.data()['current']);

    blank = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0];

    var ins1 = blank;
    var ins2 = blank;
    var ins3 = blank;
    var ins4 = blank;
    var ins5 = blank;
    var ins6 = blank;
    var ins7 = blank;

    var outs1 = blank;
    var outs2 = blank;
    var outs3 = blank;
    var outs4 = blank;
    var outs5 = blank;
    var outs6 = blank;
    var outs7 = blank;

    if(doc.exists){
    ins1 = doc.data()['inHours'];
    console.log("check");
    console.log(doc.data()['inHours'][22])
    }
    if(doc1.exists){
    ins2 = doc1.data()['inHours'];
    console.log("check");
    }
    if(doc2.exists){
    ins3 = doc2.data()['inHours'];
    console.log("check");
    }
    if(doc3.exists){
    ins4 = doc3.data()['inHours'];
    console.log("check");
    }
    if(doc4.exists){
    ins5 = doc4.data()['inHours'];
    console.log("check");
    }
    if(doc5.exists){
    ins6 = doc5.data()['inHours'];
    console.log("check");
    }
    if(doc6.exists){
    ins7 = doc6.data()['inHours'];
    console.log("check");
    }

    if(doc.exists){
    outs1 = doc.data()['outHours'];
    }
    if(doc1.exists){
    outs2 = doc1.data()['outHours'];
    }
    if(doc2.exists){
    outs3 = doc2.data()['outHours'];
    }
    if(doc3.exists){
    outs4 = doc3.data()['outHours'];
    }
    if(doc4.exists){
    outs5 = doc4.data()['outHours'];
    }
    if(doc5.exists){
    outs6 = doc5.data()['outHours'];
    }
    if(doc6.exists){
    outs7 = doc6.data()['outHours'];
    }

    console.log(ins1[0]);
    var weekins = ins1;
    var weekouts = outs1;

    for(i = 0; i < weekins.length; i++){
        weekins[i] = (ins1[i] + ins2[i] + ins4[i] + ins4[i] + ins5[i] + ins6[i] + ins7[i]);  
        weekouts[i] = (outs1[i] + outs2[i] + outs3[i] + outs4[i] + outs5[i] + outs6[i] + outs7[i]);
    }

        console.log(weekins[22]);

        $("#current").html(doc.data()['current'].toString());
        $("#entries").html(doc.data()['totalIns'].toString());
        $("#exits").html(doc.data()['totalOuts'].toString());
        $("#totalIn").html(doc.data()['lifetimeIn'].toString());
        $("#totalOut").html(doc.data()['lifetimeOut'].toString());


        var todayins = doc.data()['inHours'];
        var todayouts = doc.data()['outHours'];

        var today1 = todayins[0] + todayins[1] + todayins[2] + todayins[3] + todayins[4] + todayins[5];
        var today2 = todayins[6] + todayins[7] + todayins[8] + todayins[9] + todayins[10] + todayins[11];
        var today3 = todayins[12] + todayins[13] + todayins[14] + todayins[15] + todayins[16] + todayins[17];
        var today4 = todayins[18] + todayins[19] + todayins[20] + todayins[21] + todayins[22] + todayins[23];

        var week1 = weekins[0] + weekins[1] + weekins[2] + weekins[3] + weekins[4] + weekins[5]; 
        var week2 = weekins[6] + weekins[7] + weekins[8] + weekins[9] + weekins[10] + weekins[11];
        var week3 = weekins[12] + weekins[13] + weekins[14] + weekins[15] + weekins[16] + weekins[17];
        var week4 = weekins[18] + weekins[19] + weekins[20] + weekins[21] + weekins[22] + weekins[23];

        var data = {
        labels: ['', '', '', ''],
        series: [today1, today2, today3, today4]
        };

        var options = {
            labelInterpolationFnc: function(value) {
                return value[0]
            }
        };

    new Chartist.Pie('.ct-chart-invoice', data, options);

    var data = {
        labels: ['', '', '', ''],
        series: [week1, week2, week3, week4]
        };

        var options = {
            labelInterpolationFnc: function(value) {
                return value[0]
            }
        };

    new Chartist.Pie('.ct-chart-invoice1', data, options);
    
    
        $(function() {
        "use strict";
        // ============================================================== 
        // Product Sales
        // ============================================================== 

        new Chartist.Bar('.ct-chart-product1', {
            labels: ['00', '01', '02', '03', '04','05', '06', '07', '08','09', '10', '11', '12','13', '14', '15', '16','17', '18', '19', '20','21', '22', '23'],
            series: [todayins]
        }, {
            stackBars: false,
            axisY: {
                type: Chartist.FixedScaleAxis,
                ticks: [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100],
                low: 0
            }
        }).on('draw', function(data) {
            if (data.type === 'bar') {
                data.element.attr({
                    style: 'stroke-width: 30px'
                });
            }
        });
    });

    $(function() {
        "use strict";
        // ============================================================== 
        // Product Sales
        // ============================================================== 

        new Chartist.Bar('.ct-chart-product3', {
            labels: ['00', '01', '02', '03', '04','05', '06', '07', '08','09', '10', '11', '12','13', '14', '15', '16','17', '18', '19', '20','21', '22', '23'],
            series: [weekins]
        }, {
            stackBars: false,
            axisY: {
                type: Chartist.FixedScaleAxis,
                ticks: [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100],
                low: 0
            }
        }).on('draw', function(data) {
            if (data.type === 'bar') {
                data.element.attr({
                    style: 'stroke-width: 30px'
                });
            }
        });
    });

    $(function() {
        "use strict";
        // ============================================================== 
        // Product Sales
        // ============================================================== 

        new Chartist.Bar('.ct-chart-product4', {
            labels: ['00', '01', '02', '03', '04','05', '06', '07', '08','09', '10', '11', '12','13', '14', '15', '16','17', '18', '19', '20','21', '22', '23'],
            series: [weekouts]
        }, {
            stackBars: false,
            axisY: {
                type: Chartist.FixedScaleAxis,
                ticks: [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100],
                low: 0
            }
        }).on('draw', function(data) {
            if (data.type === 'bar') {
                data.element.attr({
                    style: 'stroke-width: 30px'
                });
            }
        });
    });

    $(function() {
        "use strict";
        // ============================================================== 
        // Product Sales
        // ============================================================== 

        new Chartist.Bar('.ct-chart-product2', {
            labels: ['00', '01', '02', '03', '04','05', '06', '07', '08','09', '10', '11', '12','13', '14', '15', '16','17', '18', '19', '20','21', '22', '23'],
            series: [todayouts]
        }, {
            stackBars: false,
            axisY: {
                type: Chartist.FixedScaleAxis,
                ticks: [0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100],
                low: 0
            }
        }).on('draw', function(data) {
            if (data.type === 'bar') {
                data.element.attr({
                    style: 'stroke-width: 30px'
                });
            }
        });
    });



    

    // ============================================================== 
    // Product Category
    // ============================================================== 
    // For the sake of the example we update the chart every time it's created with a delay of 8 seconds


  }).catch(function(error) {
  console.log("Error getting document:", error);
  });
  }).catch(function(error) {
  console.log("Error getting document:", error);
  });
  }).catch(function(error) {
  console.log("Error getting document:", error);
  });
  }).catch(function(error) {
  console.log("Error getting document:", error);
  });
  }).catch(function(error) {
  console.log("Error getting document:", error);
  });
  }).catch(function(error) {
  console.log("Error getting document:", error);
  });
   }).catch(function(error) {
  console.log("Error getting document:", error);
  });
  
