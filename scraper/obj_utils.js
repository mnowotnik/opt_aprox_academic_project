function partial(func) {
    var args = Array.prototype.slice.call(arguments, 1);
    return function() {
        var iargs = Array.prototype.slice.call(arguments);
        return func.apply(this, args.concat(iargs));
    };
};

function copy(obj, props) {
    var nobj = {};
    Object.keys(obj).forEach(function(key) {
        nobj[key] = obj[key];
    });
    if (props) {
        Object.keys(props).forEach(function(key) {
            nobj[key] = props[key];
        });
    }
    return nobj;
}

module.exports = {
    copy: copy,
    partial: partial
};
