$(document).ready(function () {

    $('#searchForm').formSearchPageRows({
        url: '/units/list-page',
        cm: [
            {col: 'unitCode'},
            {col: 'unitName'},
            {col: 'updateTime'}
        ]
    }).trigger('submit');

});