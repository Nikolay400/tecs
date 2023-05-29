function plus(obj){
    var inputField = $(obj).parent().children().eq(1);
    var count = Number($(inputField).attr("value"));
    console.log(count);
    $(inputField).attr("value",count+1);
}

function minus(obj){
    var inputField = $(obj).parent().children().eq(1);
    var count = Number($(inputField).attr("value"));
    if (count==1) return;
    console.log(count);
    $(inputField).attr("value",count-1);
}

function checkInput(obj){
      var count = parseInt($(obj).val());
      console.log("gjxtve "+count);
      if (isNaN(count)||count<1){
        $(obj).val(1);
      }else{
        $(obj).val(count);
      }
}

function deleteItem(obj){

        var token = $("meta[name='_csrf']").attr("content");
        var header = $("meta[name='_csrf_header']").attr("content");


        console.log(obj);
        let id = $(obj).attr("data-id");
        console.log("id is" + id);
        $.ajax({
            url: '/cart/'+id,         /* Куда пойдет запрос */
            method: 'DELETE',             /* Метод передачи (post или get) */
            dataType: 'html',
            beforeSend: function( xhr ) {
              xhr.setRequestHeader(header, token);
            },
            success: function(data){   /* функция которая будет выполнена после успешного запроса.  */
                console.log(data);       /* В переменной data содержится ответ от index.php. */
                location.reload();
            },
            error: function(data, errorThrown)
                      {
                          console.log('request failed :'+errorThrown);
                      }
        });
}