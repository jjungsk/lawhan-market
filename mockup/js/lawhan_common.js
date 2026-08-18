$(document).ready(function(){

	setTimeout(function(){
		sExpertise()
	}, 500)
	
	$("#menuToggle").click(function(){
        $("#lawhan_header").toggleClass("on")
    })

    $(window).scroll(function(){
		sExpertise()
        if ($(window).scrollTop() == 0){
            $("#lawhan_header").removeClass("scroll_on")
        } else {
            $("#lawhan_header").addClass("scroll_on")
        }
    })
    if ($("#sub_content").length > 0){
        $(window).scroll(function(){
            if ($(window).scrollTop() >= $("#sub_content").offset().top){
                $("#lawhan_header").addClass("bg_on")
            } else {
                $("#lawhan_header").removeClass("bg_on")
            }
        })
    }

	$("#all_gnb .menu > li > ul").each(function(){
		$(this).parent("li").addClass("depth1");
	})
	$(document).on("click", "#all_gnb .menu > li.depth1 > a", function(){
		$(this).next("ul").stop().slideToggle();
		return false;
	})


    if ($("div").hasClass("main_content")){
        scroll();
    }

    $("#mInsight .tab_btn li button").click(function(){
        $("#mInsight .tab_btn li").removeClass("active")
        $(this).parent("li").addClass("active")

        $("#mInsight .content_box > div").removeClass("active")
        $("#mInsight .content_box > div:eq("+ $(this).parent("li").index() +")").addClass("active")
    })

    $("#mInsight .content_box > div ul").each(function(){
        $(this).children("li:first").children("button").addClass("active")
        $(this).children("li:first").children("div").show()
    })
    $("#mInsight .content_box ul li button").click(function(){
        var ul = $(this).closest("ul")

        ul.children("li").children("button").removeClass("active")
        ul.children("li").children("div").stop().slideUp()

        $(this).addClass("active")
        $(this).next("div").stop().slideDown()
    })

    $("#Quick .go_top").click(function(){
        $("html, body").animate({scrollTop:0})
    })

    $("#Quick .inquiry_box button").click(function(){
        $(this).parent("div").toggleClass("active")
    })



    // sub
    $(".sContact_case2 .tab_box li button").click(function(){
        $(".sContact_case2 .tab_box li").removeClass("active")
        $(this).parent("li").addClass("active")

        $(".sContact_case2 .content_box > div").removeClass("active")
        $(".sContact_case2 .content_box > div:eq("+ $(this).parent("li").index() +")").addClass("active")
    })

	$(".sFaq_case1 ul li button").click(function(){
		$(".sFaq_case1 ul li button").not($(this)).removeClass("active");
		$(this).toggleClass("active");

		$(".sFaq_case1 ul li .cont").not($(this).next("div")).stop().slideUp();
		$(this).next("div").stop().slideToggle()
    })


	$(".sContact_case4 .box button").click(function(){
		$(".sContact_case4 .box").toggleClass("on");

		$(".sContact_case4 .box .txt_box").stop().slideToggle()
	})
})


function sExpertise(){
	$(".sExpertise_case1 .txt_box .tit").each(function(){
		if ($(this).hasClass("animated")) {
			$(this).addClass("active")
		}
	})
}


var scroll = function(){
    
    var $cnt = null,
        moveCnt = null,
        moveIndex = 0,
        moveCntTop = 0,
        winH = 0,
        time = false; // 새로 만든 변수

    $(document).ready(function(){
        init();
        initEvent();
		
		if ($("div").hasClass("main_content")){
	        $("html, body").animate({scrollTop:0})
		}

        moving(moveIndex);

        $("#Quick .go_top").click(function(){
            moveIndex = 0;
            moving(moveIndex)
        })
    });
    
    var init = function(){
        $cnt = $(".main_content");
		$btnWrap = $(".main_quick");
		$btn = $(".main_quick div");
    };
    
    var initEvent = function(){
        $("html ,body").scrollTop(0);
        winResize();
        $(window).resize(function(){
            winResize();
        });

		$btn.click(function(){
			moveIndex = $(this).index();
			moving(moveIndex);
		})

        $cnt.on("mousewheel", function(e){
			if(time === false){ // time 변수가 펄스일때만 휠 이벤트 실행
			  wheel(e.originalEvent.wheelDelta);
			}
        });
    };
        
    var winResize = function(){
        winH = $(window).height();
        $cnt.children("div").height(winH);
        $("html ,body").scrollTop(moveIndex.scrollTop);
    };
    
    var wheel = function(e){
        if(e < -20){
			if ($(window).width() > 1024){
				if(moveIndex < $cnt.children("div").length - 1){
					moveIndex += 1;
					moving(moveIndex);
				} else {
					$("html, body").animate({scrollTop:$("#lawhan_footer").offset().top})
				};
			}
        }else if (e > 20){
			if ($(window).width() > 1024){
				if(moveIndex > 0){
					moveIndex -= 1;
					moving(moveIndex);
				};
			}
        };
    };
    
    var moving = function(index){
        time = true // 휠 이벤트가 실행 동시에 true로 변경
        moveCnt = $cnt.children("div").eq(index);
        moveCntTop = moveCnt.offset().top;
        $("html ,body").stop().animate({
            scrollTop: 0
        }, 1000, function(){
          time = false; // 휠 이벤트가 끝나면 false로 변경
        });

		$btn.removeClass("active");
		$btn.eq(index).addClass("active");

		$cnt.children("div").removeClass("active");
		moveCnt.addClass("active");
        
        if (moveIndex == 1 || moveIndex == 5){
            //$("#zest_header").addClass("black")
			$btnWrap.attr("data-case", "2")
        } else if (moveIndex == 2){
            //$("#zest_header").addClass("black")
			$btnWrap.attr("data-case", "3")
        } else if (moveIndex == 3){
            //$("#zest_header").addClass("black")
			$btnWrap.attr("data-case", "4")
        } else if (moveIndex == 4){
            //$("#zest_header").addClass("black")
			$btnWrap.attr("data-case", "5")
        } else {
            //$("#zest_header").removeClass("black")
			$btnWrap.attr("data-case", "1")
        }

        if (moveIndex == 0){
            $("#lawhan_header").removeClass("scroll_on")
        } else {
            $("#lawhan_header").addClass("scroll_on")
        }

        if (moveIndex == 2 || moveIndex == 5){
            $("#lawhan_header").addClass("bg_on")
        } else {
            $("#lawhan_header").removeClass("bg_on")
        }

    };
    
};


// 이메일 검사
function inputEmail(obj){
	var reg = /^[A-Za-z0-9@.]*$/;

	if (!reg.test(obj.value)){
		obj.value = obj.value.replace(/[^A-Za-z0-9@.]*$/, "");
	}
}

// 숫자 검사
function inpuNumber(obj){
	var reg = /^[0-9]*$/;

	if (!reg.test(obj.value)){
		obj.value = obj.value.replace(/[^0-9]*$/, "");
	}
}