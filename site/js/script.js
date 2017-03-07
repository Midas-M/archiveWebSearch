/**
 * @author antska
 */

var api_url = 'http://83.212.204.92:8080/archive-1.0-SNAPSHOT/search';
// var api_url = 'http://83.212.204.92:8080/search';
var wayback_url = 'http://83.212.204.92:8080/wayback/';
var groupedData_mapped;
var groupedData_keys = {};

$(document).ready(function () {

    pageSize = 5;
    pagesCount = 0;
    currentPage = 1;
    totalPages = 0;

    // onClick event: url search button
    $('#urlsearch-button').on('click', function () {
        $('#fullsearch-button').attr('class', 'btn btn-default');
        $('#urlsearch-button').attr('class', 'btn btn-primary');
        $('#searchInput').val('');
        $('#searchInput').attr('placeholder', 'URL')

    });

    // onClick event: full search button
    $('#fullsearch-button').on('click', function () {
        $('#urlsearch-button').attr('class', 'btn btn-default');
        $('#fullsearch-button').attr('class', 'btn btn-primary');
        $('#searchInput').val('');
        $('#searchInput').attr('placeholder', 'Keywords')
    });

    // Initialize datepicker
    $('#external-container').find('.input-daterange').datepicker({
        language: "el",
        autoclose: true,
        format: "yyyy-mm-dd",
        todayHighlight: true,
        todayBtn: true
    });

    // onClick event: search link clicked -> focus on the search bar
    $('#search-bar-link').on('click', function () {
        $('#custom-search-input').focus();
        $('#searchInput').focus();
    });

    // onClick event: demo button
    $('#demo-button').on('click', function () {
        $(this).blur();
        if ($('#fullsearch-button').hasClass('active')) {
            $('#searchInput').val('eclass');
            $('input[name=start]').val('2011-01-25');
            $('input[name=end]').val('2017-02-25');
        } else if ($('#urlsearch-button').hasClass('active')) {
            $('#searchInput').val('http://www.aueb.gr/');
            $('input[name=start]').val('2011-01-25');
            $('input[name=end]').val('2017-02-25');
        }
    });

    // Keypress event: press Enter on search bar
    $('#searchInput').keypress(function (event) {
        if (event.keyCode == 13 || event.which == 13) {
            $('#search-button').click();
        }
    });

    // onClick event: more/less button
    $(document).on('click', '.plus a', function (sender) {
        var id = sender.currentTarget.parentNode.parentNode.parentNode.id;
        if (sender.currentTarget.lastElementChild.className.includes('glyphicon-plus')) {
            $('#' + id + ' .btn-group-vertical > button').removeClass('hide');
            $('#' + id + ' .excerpet > .plus > a > i').removeClass('glyphicon-plus');
            $('#' + id + ' .excerpet .plus > a > i').addClass('glyphicon-minus');
            $('#' + id + ' .excerpet .plus').get(0).lastChild.nodeValue = " Latest Edition";
        } else if (sender.currentTarget.lastElementChild.className.includes('glyphicon-minus')) {
            $('#' + id + ' .btn-group-vertical > button').addClass('hide');
            $('#' + id + ' .btn-group-vertical > button.latest').removeClass('hide');
            $('#' + id + ' .excerpet > .plus > a > i').removeClass('glyphicon-minus');
            $('#' + id + ' .excerpet .plus > a > i').addClass('glyphicon-plus');
            $('#' + id + ' .excerpet .plus').get(0).lastChild.nodeValue = " Previous Editions";
        }
    });

    // onClick event: date choose
    $(document).on('click', '.btn-group-vertical > button', function (sender) {
        var id = sender.currentTarget.parentNode.parentNode.parentNode.id;
        $('#' + id + ' button.btn.btn-default.active.latest').removeClass('active');
        $('#' + id + ' button.btn.btn-default.active.more').removeClass('active');
        $(sender.currentTarget).addClass('active');
        var sender_date = sender.currentTarget.textContent.trim();
        $('#' + id + ' > div > h3').get(0).lastChild.lastChild.nodeValue = groupedData_mapped[groupedData_keys[id]][sender_date].title;
        $('#' + id + ' > div > p').empty();
        $('#' + id + ' > div > p').append(groupedData_mapped[groupedData_keys[id]][sender_date].content.substring(0, 200) + '...');
        $('#' + id + ' > div > h3 a').get(0).href = groupedData_mapped[groupedData_keys[id]][sender_date].wayback_url;
        $('#' + id + ' > div > h3 a').get(0).title = groupedData_mapped[groupedData_keys[id]][sender_date].title;
    });

    // onClick event: Search button
    $('#search-button').on('click', function () {
        var searchValue = $('input[name=searchInput]').val();
        var datefrom = $('input[name=start]').val();
        var dateto = $('input[name=end]').val();

        $('.search-result').remove();

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
                    $.get(api_url, {keywords: keywords}, function (data) {
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
                        }).on("page", function (event, num) {
                            $(".search-result.row.normal").hide().each(function (n) {
                                if (n >= pageSize * (num - 1) && n < pageSize * num)
                                    $(this).show();
                            });
                        });
                        $('#results').loading('stop');
                    });
                }
                else {
                    $.get(api_url, {keywords: keywords, datefrom: datefrom, dateto: dateto}, function (data) {

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
                        }).on("page", function (event, num) {
                            $(".search-result.row.normal").hide().each(function (n) {
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
                $.get(api_url, {url: url_input, datefrom: datefrom, dateto: dateto}, function (data) {
                    createResults(data);
                    $('.plus a').click();
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
                    }).on("page", function (event, num) {
                        $(".search-result.row.normal").hide().each(function (n) {
                            if (n >= pageSize * (num - 1) && n < pageSize * num)
                                $(this).show();
                        });
                    });
                });
                $('#results').loading('stop');
            }
        }
    });
});


function createResults(data) {
    var jsonObject = JSON.parse(data);
    // var jsonObject_temp = JSON.parse(JSON.stringify(jsonObject));
    var obj = {};
    obj.items = [];
    for (var j = 0; j < jsonObject.items.length; j++) {
        obj.items.push(jsonObject.items[j]);
        // testing...
        // jsonObject_temp.items[j].date = '2011-11-01T19:16:11Z';
        // jsonObject_temp.items[j].title = 'Sample Title';
        // jsonObject_temp.items[j].content = 'Sample Content Sample Content Sample Content Sample Content Sample Content ';
        // obj.items.push(jsonObject_temp.items[j]);
    }
    var groupedData = _.groupBy(obj.items, function (d) {
        return d.url;
    });
    groupedData_mapped = _.object(_.map(groupedData, function (group, uri) {
        var temp_j = {};
        group = group.sort();
        for (var g in group) {
            temp_j[group[g].date.split('T')[0]] = group[g];
        }
        return [uri, temp_j];
    }));
    var id = 0;
    for (var uri in groupedData_mapped) {
        var uri_id = "uri_" + id;
        groupedData_keys[uri_id] = uri;
        var dates = [];
        if (groupedData_mapped.hasOwnProperty(uri)) {

            var first_d_id = Object.keys(groupedData_mapped[uri])[0];
            var url = groupedData_mapped[uri][first_d_id].url.toString();
            var title = groupedData_mapped[uri][first_d_id].title.toString();
            var content = groupedData_mapped[uri][first_d_id].content.toString();

            for (var d_id in groupedData_mapped[uri]) {
                if (groupedData_mapped[uri].hasOwnProperty(d_id)) {
                    var date = groupedData_mapped[uri][d_id].date.toString();
                    dates.push(date);
                    var wayback = wayback_url + date.split('T')[0].replace(/-/g, '') + date.split('T')[1].replace(/:/g, '').slice(0, -1) + '/';
                    groupedData_mapped[uri][d_id].wayback_url = wayback + url;
                }
            }
            date = groupedData_mapped[uri][first_d_id].date.toString();
            wayback = wayback_url + date.split('T')[0].replace(/-/g, '') + date.split('T')[1].replace(/:/g, '').slice(0, -1) + '/';


            $('#results').append('<article id="' + uri_id + '" class="search-result row normal">' +
                '<div class="col-xs-12 col-sm-12 col-md-2">' +
                '<div class="btn-group-vertical" role="group">' +
                '<button type="button" class="btn btn-default active latest">' +
                '<i class="glyphicon glyphicon-calendar"></i> ' + dates[0].split('T')[0] +
                '</button>' +
                '</div>' +
                '</div>' +
                '<div class="col-xs-12 col-sm-12 col-md-7 excerpet">' +
                '<h3><a href=' + wayback + url + ' title="' + title + '" target="_blank">' + title + '</a></h3>' +
                '<p>' + content.substring(0, 200) + '...</p>' +
                '</article>');
            if (dates.length > 1) {
                $('#' + uri_id + ' .excerpet').append('<span class="plus"><a href="javascript:void(0)" title="More"><i class="glyphicon glyphicon-plus"></i></a> Previous Editions</span>');
            }
            $('#' + uri_id).append('<span class="clearfix borda"></span>');

            for (var d = 1; d < dates.length; d++) {
                $('#' + uri_id + ' .btn-group-vertical').append('<button type="button" class="btn btn-default more hide">' +
                    '<i class="glyphicon glyphicon-calendar"></i> ' + dates[d].split('T')[0] +
                    '</button>');
            }
        }
        id++;
    }

    $('#results').find('article').last().append('<span class="clearfix border"></span>');

    $(".search-result.row.normal").hide().each(function (n) {
        if (n < 5)
            $(this).show();
    });
}
