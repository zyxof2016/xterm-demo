window.onload = function (ev) {
    if ('WebSocket' in window) {
        createTerminal("ws://localhost:9300/testWebSocket");
    }else {
        alert('当前浏览器 Not support websocket');
    }
}
createTerminal = function(linkpath) {
    var ws = new WebSocket(linkpath);
    var xterm = new Terminal({
        cols: 100,
        rows: 20,
        cursorBlink: 5,
        scrollback: 30,
        tabStopWidth: 4
    });  // Instantiate the terminal
    xterm.open(document.getElementById('terminal'));
    ws.onerror = function () { showErrorMessage('connect error.') };
    ws.onmessage = function(event) {
        console.log('on message:',that.decodeBase64Content(event.data))
        xterm.write(that.decodeBase64Content(event.data));
    };
    ws.onopen = function () {
        console.log('ws onopen ')
    }
    console.log(xterm.element.classList);

    xterm.textarea.onkeydown = function (e) {
        console.log('User pressed key with keyCode: ', e.keyCode);
        //console.log('编码',)
        //ws.send(that.encodeBase64Content(e.keyCode.toString()));
        //ws.send('bHM=');
    }
    xterm.attachCustomKeyEventHandler(function (e) {
        if (e.keyCode == 13) {
            console.log('enter')
            ws.send('DQ==')
            return false;
        }
    });
    xterm.on('data',function(data){
        console.log('data xterm=>',data)
        //xterm.write(data);
        ws.send(that.encodeBase64Content(data.toString()))
    })

    xterm.on('output', function (data) {
        console.log('output===',data)
        xterm.write(data);
    });

    xterm.on('blur', function (data) {
        console.log('blur===',arrayBuffer)
        xterm.write(arrayBuffer);
    });

    xterm.on('focus', function (data) {
        console.log('focus===',arrayBuffer)
        xterm.write(arrayBuffer);
    });

    xterm.on('keydown', function (data) {
        console.log('keydown===',arrayBuffer)
        xterm.write(arrayBuffer);
    });

    xterm.on('lineFeed', function (data) {
        console.log('lineFeed===',arrayBuffer)
        xterm.write(arrayBuffer);
    });

    xterm.on('resize', function (data) {
        ws.send('resize', [data.cols, data.rows]);
        console.log('resize', [data.cols, data.rows]);
    })
}
var decodeBase64Content = function(base64Content) {
    // base64 解码
    var commonContent = base64Content.replace(/\s/g, '+');
    commonContent = Buffer.from(commonContent, 'base64').toString();
    return commonContent;
}
var encodeBase64Content= function(commonContent) {
    // base64 编码
    var base64Content = Buffer.from(commonContent).toString('base64');
    return base64Content;
}
