function changeImg(obj){
    var imgName = $(obj).attr("src");
    $(obj).parent().find(".border-primary").removeClass("border").removeClass("border-primary");
    $(obj).addClass("border").addClass("border-primary");
    $("#main-img").attr("src",imgName);
}


function plusProduct(obj){
    if ($(obj).hasClass("no")){
        let id = parseInt($(obj).attr("data-id"));
        addProduct(id,1,obj,true);
    }else{
        let id = parseInt($(obj).parent().attr("data-id"));
        addProduct(id,1,obj);
    }
}

function minusProduct(obj){
    var midField = $(obj).parent().children().eq(1);
    var count = parseInt($(midField).html());
    let id = parseInt($(obj).parent().attr("data-id"));
    if (count<=1) {
        delCart(id,obj);
        return;
    }
    addProduct(id,-1,obj);
}

function addProduct(id,q,obj,isFirst=false){

    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    $.ajax({
        url: '/add-product-to-cart',         /* Куда пойдет запрос */
        method: 'post',             /* Метод передачи (post или get) */
        dataType: 'html',
        beforeSend: function( xhr ) {
          xhr.setRequestHeader(header, token);
        },
        data: {'productId': id,"quantity":q},     /* Параметры передаваемые в запросе. */
        success: function(data){   /* функция которая будет выполнена после успешного запроса.  */
            var count = Number($("#countInCart").text());
            console.log("Count - "+count);
            $("#countInCart").text(count+q);
            var par;
            if (isFirst){
                par = $(obj).parent();
                $(par).find(".yes").removeClass("d-none");
                $(par).find(".yes").addClass("d-flex");
                $(par).find(".no").removeClass("d-block");
                $(par).find(".no").addClass("d-none");
            }else{
                par = $(obj).parent().parent();
            }

            var midField = $(par).find(".yes").children().eq(1);
            var count = parseInt($(midField).html());
            $(midField).html(count+q);
        },
        error: function(data, errorThrown)
                  {
                      console.log('request failed :'+errorThrown);
                  }
    });
}

function delCart(id,obj){

    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    $.ajax({
        url: '/del-cart',         /* Куда пойдет запрос */
        method: 'post',             /* Метод передачи (post или get) */
        dataType: 'html',
        beforeSend: function( xhr ) {
          xhr.setRequestHeader(header, token);
        },
        data: {'productId': id},     /* Параметры передаваемые в запросе. */
        success: function(data){   /* функция которая будет выполнена после успешного запроса.  */
            var count = Number($("#countInCart").text());
            console.log("Count - "+count);
            $("#countInCart").text(count-1);
            var par = $(obj).parent().parent();
            var midField = $(par).find(".yes").children().eq(1);
            var count = parseInt($(midField).html());
            $(midField).html(count-1);
            $(par).find(".yes").addClass("d-none");
            $(par).find(".yes").removeClass("d-flex");
            $(par).find(".no").removeClass("d-none");
            $(par).find(".no").addClass("d-block");
        },
        error: function(data, errorThrown)
              {
                  console.log('request failed :'+errorThrown);
              }
    });
}

