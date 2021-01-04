$(document).ready(function () {

    $('#searchForm').formSearchPageRows({
        url: '/targets/list-page',
        cm: [
            {col: 'probeCode'},
            {col: 'equipCode'},
            {col: 'targetCode'},
            {col: 'gatewayCode'},
            {col: 'frequncey'},
            {col: 'pointCode'},
            {col: 'pointName'},
            {col: 'unitCode'},
            {col: 'unitName'},
            {
                col: function (obj) {
                    return $.buttonActions([
                        {text: '编辑', click: edit},
                        {text: '删除', click: remove}
                    ], obj);
                }
            }
        ]
    }).trigger('submit');

    var refreshRows = function () {
        $('#searchForm').formSearchPageRows('search');
    };

    var add = function () {
        $('input[name=probeCode]',$('#dataForm')).attr("readonly", false);
        $('#dataForm').modalPost('/targets', function () {
            $.alert('保存成功！', refreshRows);
        });
    };
    $('#addButton').on('click', add);

    var edit = function (obj) {
        $('input[name=probeCode]',$('#dataForm')).attr("readonly", true);
        $('#dataForm').modalPut('/targets/' + obj.probeCode, obj, function () {
            $.alert('保存成功！', refreshRows);
        });
    };

    var remove = function (obj) {
        $.confirmDelete('/targets/' + obj.probeCode, function () {
            $.alert('删除成功！', refreshRows);
        });
    };

    //导入Prober
    $('#importBtn').on('click', function () {
        var file = document.getElementById("inputFile").files[0];
        if (file == undefined) {
            $.alert("请先选择Excel文件...");
        } else if (file.name.lastIndexOf(".xls") < 0) {//可判断以.xls和.xlsx结尾的excel  
            $.alert("只能上传Excel文件");
        } else if (file.size > 10 * 1024 * 1024) {
            $.alert("文件大小不能超过10M!");
        } else {
            var formData = new FormData($('#fileForm')[0]);
            $.ajax({
                url: '/targets/import',
                type: 'post',
                data: formData,
                cache: false,
                contentType: false,
                processData: false
            }).done(function () {
                $('#fileModal').modal('hide');
                $.alert("导入成功！", refreshRows);
            });
        }
    });

    $('#tempFileDownload').on('click', function () {
        window.open(contextPath + '/targets/temp-file');
    });

    $('#inputFile').on('change', function(e){
        var fileName = (e.target.value != undefined && e.target.value != null && e.target.value != '')
            ? e.target.value.substr(e.target.value.lastIndexOf("\\") + 1, e.target.value.length) : '请选择可用文件';
        $('.custom-file-label').html(fileName);
    });

});