var fs = require('fs');

function csvWriter(path, delim, headers) {
    var ostream = fs.open(path, 'w');

    function toRow(arr) {
        var l = arr.length;
        var row = '';
        for (var i = 0; i < l; i++) {
            if (i > 0) {
                row += delim;
            }
            row += arr[i];
        }
        return row + '\n';
    };

    ostream.write(toRow(headers));
    return {

        close: function() {
            ostream.close();
        },
        flush: function() {
            ostream.flush();
        },
        write: function(str) {
            ostream.write(str);
        },
        writeLine: function(line) {
            ostream.write(line + '\n');
        },
        writeArr: function(data) {
            ostream.write(toRow(data));
        },
        writeMap: function(obj) {
            row = [];
            var l = headers.length;
            for (var i = 0; i < l; i++) {
                row.push(obj[headers[i]]);
            }
            this.writeArr(row);
        }
    }
};

module.exports = csvWriter;
