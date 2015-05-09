//run : casperjs --engine=slimerjs script.js
//
var jQueryUrl = "https://ajax.googleapis.com/ajax/libs/jquery/2.1.3/jquery.min.js"

var fs = require('fs');
var casper = require('casper').create({
    remoteScripts: [jQueryUrl],
    // pageSettings: {
    //     loadImages: false,
    //     loadPlugins: false
    // },
    logLevel: "info",
    // verbose: true,
});

var url = 'http://msg.szewczyk.pro';
var credentials = require('./user')
var login = credentials.username;
var pass = credentials.password;

casper.start(url)

casper.waitForSelector('#loginButton', function() {
    this.evaluate(function(login, password) {
        $("#j_username").val(login);
        $("#j_password").val(password);
        $("#loginButton").click();
    }, login, pass);
});

function cycle(ctx) {

    casper.waitForSelector('#addNewGameLink', function() {
        this.thenOpen(url + '/#!add-new-game');
    });

    casper.wait(2000, function() {
        var game = this.evaluate(function() {
            $("#playersInput").val('1').focus()
                .blur().trigger('change');
            var val = $('#nameInput').val();
            $('#addNewGameButton')[0].click();
            return val;
        });
        ctx.game = game;
    });

    casper.waitForSelector('#addNewGameLink', function() {
        this.evaluate(function(game) {
            $("[id='join-" + game + "']")[0].click();
        }, ctx.game);

    });

    casper.wait(3000, function() {
        this.evaluate(function() {
            $('div[class="v-caption"]:contains(Decyzje)')[0].click();

        });
        this.wait(1000);
    });

    setQuality('100');
    setVolume('1');
    casper.then(
        function() {
            casper.click('input[tabindex="1"]');
        }
    );

    var ostream = fs.open('vol_qual100_uprice.csv', 'w');
    var l = ctx.values.length;
    var values = ctx.values;
    for (var i = 0; i < l; i++) {
        var val = values[i];
        setVolume(val.toString());
        readData();

        casper.then(function() {
            var vec = ctx.vec;
            this.echo(vec[0] + " " + vec[1] + " " + vec[2]);
            ostream.write(vec[0] + " " + vec[1] + " " + vec[2] + '\n');
        });

    }
    casper.then(function() {
        ostream.close();
    });
};

function setParam(selector, val) {
    var thenFunc = function(selector, val) {
        this.evaluate(function(selector, val) {
            $(selector)[0].value = val;
        }, selector, val);
        this.click(selector);
    };
    this.then(thenFunc.bind(this, selector, val));
    this.wait(1700);
}

function readData() {
    this.then(function() {
        var vec = this.evaluate(function() {
            return [$('input[tabindex=1]')[0].value,
                $('input[tabindex=2]')[0].value,
                $('[tabindex="0"]')[3].value
            ];
        });
        ctx.vec = vec;
    });
};

function partial(func) {
    var args = Array.prototype.slice.call(arguments, 1);
    return function() {
        var iargs = Array.prototype.slice.call(arguments);
        return func.apply(this, args.concat(iargs));
    };
};

var setQuality = partial(setParam, 'input[tabindex="2"]').bind(casper);
var setVolume = partial(setParam, 'input[tabindex="1"]').bind(casper);
readData = readData.bind(casper);

var values = [];
for(var i=1;i<200000;i+=100){
    values.push(i);
}

var ctx = {values:values};
cycle(ctx);
casper.run();
