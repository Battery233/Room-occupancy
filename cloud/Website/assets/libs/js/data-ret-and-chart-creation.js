//firebase links
src="https://www.gstatic.com/firebasejs/5.8.4/firebase-app.js"
src="https://www.gstatic.com/firebasejs/5.8.4/firebase-auth.js"
src="https://www.gstatic.com/firebasejs/5.8.4/firebase-database.js"
src="https://www.gstatic.com/firebasejs/5.8.4/firebase-firestore.js"
src="https://www.gstatic.com/firebasejs/5.8.4/firebase-messaging.js"
src="https://www.gstatic.com/firebasejs/5.8.4/firebase-functions.js"

//firebase key
 const firebaseApp = firebase.initializeApp({
    apiKey: "AIzaSyDKV3x9atqHq6_Nbjxz8KkC_kijK7Clcno",
    authDomain: "roomoccupancy-39040.firebaseapp.com",
    databaseURL: "https://roomoccupancy-39040.firebaseio.com",
    projectId: "roomoccupancy-39040",
    storageBucket: "roomoccupancy-39040.appspot.com",
    messagingSenderId: "803591973096"
});



//firestore database declaration

var db = firebaseApp.firestore();

//get date of today and convert to string to match document names
  var d1 = new Date(); 
  var dateString1 =
    d1.getUTCFullYear() +
    ("0" + (d1.getUTCMonth()+1)).slice(-2) +
    ("0" + d1.getUTCDate()).slice(-2);
    //create firestore object
    const firestore = firebase.firestore();
    const settings = {timestampsInSnapshots: true};
    firestore.settings(settings);
    //get data from the collection "processed" with document names today's date
    var docRef = db.collection("processed").doc(dateString1);
    //.then to use data in sequential execution
    docRef.get().then(function(doc) {

        //set jquery calues from the data for all single values
        $("#current").html(doc.data()['current'].toString());
        $("#entries").html(doc.data()['totalIns'].toString());
        $("#exits").html(doc.data()['totalOuts'].toString());
        $("#totalIn").html(doc.data()['lifetimeIn'].toString());
        $("#totalOut").html(doc.data()['lifetimeOut'].toString());


        var todayins = doc.data()['inHours'];
        var todayouts = doc.data()['outHours'];
        var weekins = doc.data()['weekIns'];
        var weekouts = doc.data()['weekOuts'];

        //create quarter values for pie chart

        var today1 = todayins[0] + todayins[1] + todayins[2] + todayins[3] + todayins[4] + todayins[5];
        var today2 = todayins[6] + todayins[7] + todayins[8] + todayins[9] + todayins[10] + todayins[11];
        var today3 = todayins[12] + todayins[13] + todayins[14] + todayins[15] + todayins[16] + todayins[17];
        var today4 = todayins[18] + todayins[19] + todayins[20] + todayins[21] + todayins[22] + todayins[23];

        var week1 = weekins[0] + weekins[1] + weekins[2] + weekins[3] + weekins[4] + weekins[5]; 
        var week2 = weekins[6] + weekins[7] + weekins[8] + weekins[9] + weekins[10] + weekins[11];
        var week3 = weekins[12] + weekins[13] + weekins[14] + weekins[15] + weekins[16] + weekins[17];
        var week4 = weekins[18] + weekins[19] + weekins[20] + weekins[21] + weekins[22] + weekins[23];


        //pie chart for data of today only
        var data = {
        labels: ['', '', '', ''],
        series: [today1, today2, today3, today4]
        };

        var options = {
            labelInterpolationFnc: function(value) {
                return value[0]
            }
        };

        //set jquery assignment for today's chart

    new Chartist.Pie('.ct-chart-invoice', data, options);


        //pie chart for data of the last 7 days
    var data = {
        labels: ['', '', '', ''],
        series: [week1, week2, week3, week4]
        };

        var options = {
            labelInterpolationFnc: function(value) {
                return value[0]
            }
        };

        //set jquery assignment for last 7 days pie chart
    new Chartist.Pie('.ct-chart-invoice1', data, options);
    
    
    //creat bar chart for all the entraces today only(by hour)
        $(function() {
        "use strict";
            //set jquery name
        new Chartist.Bar('.ct-chart-product1', {
            labels: ['00', '01', '02', '03', '04','05', '06', '07', '08','09', '10', '11', '12','13', '14', '15', '16','17', '18', '19', '20','21', '22', '23'],
            series: [todayins]
        }, {
            stackBars: false,
            axisY: {
                type: Chartist.FixedScaleAxis,
                ticks: [0, 1, 2, 3, 4, 5, 10, 15,20,25,30,35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100],
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

        //create bar chart for all the entraces for the last 7 days(by hour)
    $(function() {
        "use strict";
        //set jquery name
        new Chartist.Bar('.ct-chart-product3', {
            labels: ['00', '01', '02', '03', '04','05', '06', '07', '08','09', '10', '11', '12','13', '14', '15', '16','17', '18', '19', '20','21', '22', '23'],
            series: [weekins]
        }, {
            stackBars: false,
            axisY: {
                type: Chartist.FixedScaleAxis,
                ticks: [0, 5, 10, 15,20,25,30,35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100,105, 110, 115, 120, 125, 130, 135, 140, 145, 150, 155, 160, 165, 170, 185, 190, 195, 200 ],
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
        //creat bar chart for all the exits in the last 7 days (by hour)
    $(function() {
        "use strict";
            //set jquery name
        new Chartist.Bar('.ct-chart-product4', {
            labels: ['00', '01', '02', '03', '04','05', '06', '07', '08','09', '10', '11', '12','13', '14', '15', '16','17', '18', '19', '20','21', '22', '23'],
            series: [weekouts]
        }, {
            stackBars: false,
            axisY: {
                type: Chartist.FixedScaleAxis,
                ticks: [0, 5, 10, 15,20,25,30,35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100,105, 110, 115, 120, 125, 130, 135, 140, 145, 150, 155, 160, 165, 170, 185, 190, 195, 200 ],
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


        //create bar chat for all the exits today only(by hour)
    $(function() {
        "use strict";
            //set jquery name
        new Chartist.Bar('.ct-chart-product2', {
            labels: ['00', '01', '02', '03', '04','05', '06', '07', '08','09', '10', '11', '12','13', '14', '15', '16','17', '18', '19', '20','21', '22', '23'],
            series: [todayouts]
        }, {
            stackBars: false,
            axisY: {
                type: Chartist.FixedScaleAxis,
                ticks: [0, 1, 2, 3, 4, 5, 10, 15,20,25,30,35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100],
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


  }).catch(function(error) {
  console.log("Error getting document:", error);
  });
  
