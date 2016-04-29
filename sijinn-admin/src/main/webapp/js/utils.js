function createLineElement(id, x, y, length, angle) {
    var line = document.createElement("div");
    line.setAttribute('id',id);
    var styles = 'border: 1px ridge #87C5F0; '
               + 'width: ' + length + 'px; '
               + 'height: 0px; '
               + '-moz-transform: rotate(' + angle + 'rad); '
               + '-webkit-transform: rotate(' + angle + 'rad); '
               + '-o-transform: rotate(' + angle + 'rad); '  
               + '-ms-transform: rotate(' + angle + 'rad); '  
               + 'position: absolute; '
               + 'top: ' + y + 'px; '
               + 'left: ' + x + 'px; '
    		   + 'opacity: 0.5; '
    		   + 'filter: alpha(opacity=50); '
    		   + '-moz-box-shadow: 1px 1px 2px gray; '
    		   + '-webkit-box-shadow: 1px 1px 2px gray; '
    		   + 'box-shadow: 1px 1px 2px gray; ';

    line.setAttribute('style', styles);  
    return line;
}

function createLine(id, x1, y1, x2, y2) {
    var a = x1 - x2,
        b = y1 - y2,
        c = Math.sqrt(a * a + b * b);

    var sx = (x1 + x2) / 2,
        sy = (y1 + y2) / 2;

    var x = sx - c / 2,
        y = sy;

    var alpha = Math.PI - Math.atan2(-b, a);

    return createLineElement(id, x, y, c, alpha);
}

