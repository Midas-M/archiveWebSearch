/**
 * @author antska
 */

var url = 'http://83.212.204.92:8080/search';

$(document).ready(function () {

    pageSize = 5;
    pagesCount = 0;
    currentPage = 1;
    totalPages = 0;

    $('#urlsearch-button').on('click', function () {
        $('#fullsearch-button').attr('class', 'btn btn-default');
        $('#urlsearch-button').attr('class', 'btn btn-primary');
        $('#searchInput').val('');
        $('#searchInput').attr('placeholder', 'URL')

    });
    $('#fullsearch-button').on('click', function () {
        $('#urlsearch-button').attr('class', 'btn btn-default');
        $('#fullsearch-button').attr('class', 'btn btn-primary');
        $('#searchInput').val('');
        $('#searchInput').attr('placeholder', 'Keywords')
    });

    $('#external-container').find('.input-daterange').datepicker({
        language: "el",
        autoclose: true,
        format: "yyyy-mm-dd",
        todayHighlight: true,
        todayBtn: true
    });

    $('#search-bar-link').on('click', function () {
        $('#custom-search-input').focus();
        $('#searchInput').focus();
    });

    $('#demo-button').on('click', function () {
        $(this).blur();
        if ($('#fullsearch-button').hasClass('active')) {
            $('#searchInput').val('computer science');
            $('input[name=start]').val('2016-01-25');
            $('input[name=end]').val('2017-02-25');
        } else if ($('#urlsearch-button').hasClass('active')) {
            $('#searchInput').val('http://www.aueb.gr/');
            $('input[name=start]').val('2016-01-25');
            $('input[name=end]').val('2017-02-25');
        }
    });

    $('#searchInput').keypress(function (event) {
        if (event.keyCode == 13 || event.which == 13) {
            $('#search-button').click();
        }
    });

    $('#search-button').click(function () {
        var searchValue = $('input[name=searchInput]').val();
        var datefrom = $('input[name=start]').val();
        var dateto = $('input[name=end]').val();

        if ($('#fullsearch-button').hasClass('active')) {
            var keywords = searchValue.replace(' ', ',');
            if (keywords == '') {
                $.notify({
                    icon: 'glyphicon glyphicon-warning-sign',
                    title: 'Error!',
                    message: 'Please enter some keywords'
                }, {
                    type: 'danger',
                    delay: 0,
                    allow_dismiss: true,
                    placement: {
                        from: "top",
                        align: "center"
                    },
                    animate: {
                        enter: 'animated pulse',
                        exit: 'animated fadeOutUp'
                    }
                });
            } else {
                $('.search-result.row.demo').hide();
                $('#results').loading({start: true, theme: 'transparent'});

                if (datefrom == '' || dateto == '') {
                    $.get(url, {keywords: keywords}, function (data) {
                        alert(data);
                        createResults(data);
                    });
                }
                else {
                    $.get(url, {keywords: keywords, datefrom: datefrom, dateto: dateto}, function (data) {
                        //alert(data);
                        $('.lead').show();
                        $('#input-text').text(keywords);

                        createResults(data);
                        pagesCount = $(".search-result.row.normal").length;
                        $('#number-results').text(pagesCount);
                        var totalPages = Math.ceil(pagesCount / pageSize);
                        $('.top-pagination,.bottom-pagination').bootpag({
                            total: totalPages,
                            page: 1,
                            maxVisible: 5,
                            leaps: true,
                            firstLastUse: true,
                            first: '←',
                            last: '→',
                            wrapClass: 'pagination',
                            activeClass: 'active',
                            disabledClass: 'disabled',
                            nextClass: 'next',
                            prevClass: 'prev',
                            lastClass: 'last',
                            firstClass: 'first'
                        }).on("page", function(event, num){
                            $(".search-result.row.normal").hide().each(function(n) {
                                if (n >= pageSize * (num - 1) && n < pageSize * num)
                                    $(this).show();
                            });
                        });
                        $('#results').loading('stop');
                    });
                }
            }
        } else if ($('#urlsearch-button').hasClass('active')) {
            var url_input = searchValue.trim();
            if (url_input == '') {
                $.notify({
                    icon: 'glyphicon glyphicon-warning-sign',
                    title: 'Error!',
                    message: 'Please enter a URL'
                }, {
                    type: 'danger',
                    delay: 1000,
                    allow_dismiss: true,
                    placement: {
                        from: "top",
                        align: "center"
                    },
                    animate: {
                        enter: 'animated pulse',
                        exit: 'animated fadeOutUp'
                    }
                });
            } else {
                $('.search-result.row.demo').hide();
                $('#results').loading();
                //    code for opening WAYBACK (API)
            }
        }
    });
});

function getData(data) {
    // parse JSON data
    // loop through data
    // add elements to id='results'
    // ....
}

function createResults(data) {
    var jsonObject = JSON.parse(data);
    for (var i = 0; i < jsonObject.items.length; i++) {
        var date = jsonObject.items[i].date.toString();
        var url = jsonObject.items[i].url.toString();
        var title = jsonObject.items[i].title.toString();
        var content = jsonObject.items[i].content.toString();

        $('#results').append('<article class="search-result row normal">' +
            '<div class="col-xs-12 col-sm-12 col-md-2">' +
            '<ul class="meta-search">' +
            '<li><i class="glyphicon glyphicon-calendar"></i><span>'+date.split('T')[0]+'</span></li>' +
            '</ul>' +
            '</div>' +
            '<div class="col-xs-12 col-sm-12 col-md-7 excerpet">' +
            '<h3><a href='+url+' title="">'+title+'</a></h3>' +
            '<p>'+content.substring(0,200)+'...</p>' +
            '<span class="plus"><a href="#" title="More"><i class="glyphicon glyphicon-plus"></i></a></span>' +
            '</div>' +
            '<span class="clearfix borda"></span>' +
            '</article>')
    }

    $('#results').find('article').last().append('<span class="clearfix border"></span>')

    $(".search-result.row.normal").hide().each(function(n) {
        if (n < 5)
            $(this).show();
    });
}
