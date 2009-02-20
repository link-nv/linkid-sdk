function startProgress() {
    $("#progressform .bar").show();
    setTimeout(function() {
        $("#progressform .bar").hide();
        $("#progressform").get(0).submit();
    }, 4000);
}