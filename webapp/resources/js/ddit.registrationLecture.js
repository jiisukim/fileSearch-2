
/**
 * 수강신청 / 장바구니 / 수강완료 페이지에서 공통으로 사용하는 함수
 */
 
 // 페이징 이동
function fn_movePage(pageNo) {
	$("#pageNo").val(pageNo); // 이동할 페이지 번호를 넣고
	$("#frm").submit(); // list를 가져오기
}
 
 // 강의 계획서 열람하기
function fn_showSyllabus(lectOpenNum) {
	$.ajax({
		type:"POST"
		,url:"/student/registrationLecture/getSyllabus"
		,contentType: "application/json; charset=UTF-8"
		,data: JSON.stringify({'lectOpenNum' : lectOpenNum})
		,dataType: "json"
		,success: function(data) {
			fn_makeModal(data); // 모달 창에 데이터 뿌리기
		}
	});	
}
 
 // 강의 계획서에 값 넣기
 function fn_makeModal(syllabusVo) {
		$("#email").val(syllabusVo.email);
		$("#phNum").val(syllabusVo.phNum);
		$("#subjNum").val(syllabusVo.subjNum);
		$("#nameModal").val(syllabusVo.name);
		$("#divideNum").val(syllabusVo.divideNum);
		$("#univDeptNameModal").val(syllabusVo.univDeptName);
		$("#lectMethod").val(syllabusVo.lectMethod);
		$("#univDeptName").val(syllabusVo.univDeptName);
		$("#subjTypeCodeName").val(syllabusVo.subjTypeCodeName);
		$("#lectNameModal").val(syllabusVo.lectName);
		$("#lectOvr").val(syllabusVo.lectOvr);
		$("#lectGoal").val(syllabusVo.lectGoal);
		$("#midReflectPer").val(syllabusVo.midReflectPer);
		$("#finalReflectPer").val(syllabusVo.finalReflectPer);
		$("#attendReflectPer").val(syllabusVo.attendReflectPer);
		$("#assignRelectPer").val(syllabusVo.assignRelectPer);
		$("#quizReflectPer").val(syllabusVo.quizReflectPer);
		$("#debateReflectPer").val(syllabusVo.debateReflectPer);
		$("#otherReflectPer").val(syllabusVo.otherReflectPer);
		$("#mainTxtb").val(syllabusVo.mainTxtb);
		$("#secTxtb").val(syllabusVo.secTxtb);
		$("#w1LectPlan").val(syllabusVo.w1LectPlan);
		$("#w2LectPlan").val(syllabusVo.w2LectPlan);
		$("#w3LectPlan").val(syllabusVo.w3LectPlan);
		$("#w4LectPlan").val(syllabusVo.w4LectPlan);
		$("#w5LectPlan").val(syllabusVo.w5LectPlan);
		$("#w6LectPlan").val(syllabusVo.w6LectPlan);
		$("#w7LectPlan").val(syllabusVo.w7LectPlan);
		$("#w8LectPlan").val(syllabusVo.w8LectPlan);
		$("#w9LectPlan").val(syllabusVo.w9LectPlan);
		$("#w10LectPlan").val(syllabusVo.w10LectPlan);
		$("#w11LectPlan").val(syllabusVo.w11LectPlan);
		$("#w12LectPlan").val(syllabusVo.w12LectPlan);
		$("#w13LectPlan").val(syllabusVo.w13LectPlan);
		$("#w14LectPlan").val(syllabusVo.w14LectPlan);
		$("#w15LectPlan").val(syllabusVo.w15LectPlan);
		$("#w16LectPlan").val(syllabusVo.w16LectPlan);
		$("#otherthings").val(syllabusVo.otherthings);
	}
	
	// 장바구니에서 제거
	function fn_cartDelete(lectOpenNum) {
		// frm의 action 바꿔 요청
		$("#frm").prop("action", "/student/registrationLecture/cartDelete");
		$("#lectOpenNum").val(lectOpenNum);
		$("#frm").submit();
	}
	
	// 수강 신청 담기
	function fn_regLectInsert(index, flag) {
		
		var jsonList = [];
		
		if(flag == 1){
			jsonList = jsonLectureOpenList;
		}else if(flag == 2){
			jsonList = jsonCartList;
		}
		
		if(!confirm("[" + jsonList[index].lectName + "] 강의를 수강 신청하시겠습니까?")){
			return;
		}
				
		// 신청 학점 넘었는지 확인
		if(fn_isOverCredAvail(jsonList[index].cred)){
			alert("[" + jsonList[index].lectName +  "] : 수강 신청 실패했습니다. 수강 신청 가능 학점 범위를 벗어났습니다.");
			return;
		};
		
		// 같은 시간대 강의가 있는지 확인 (만들어야 함)
		if(fn_isDuplTime(jsonList[index].lectTime)){
			alert("[" + jsonList[index].lectName +  "] : 수강 신청 실패했습니다. 강의 시간이 중복입니다.");
			return;
		}
		
		$("#lectOpenNum").val(jsonList[index].lectOpenNum);
		$("#univDeptNum").val(jsonList[index].univDeptNum);
		$("#grdtnRequYn").val(jsonList[index].grdtnRequYn);
		
		$("#frm").prop("action", "/student/registrationLecture/registrationLectureInsert");
		$("#frm").submit();
	}
	
	
	// 신청 학점 넘었는지 확인
	function fn_isOverCredAvail(credStr) {
		var cred = parseInt(credStr);
		var credSum = parseInt($("#credSum").text());
		var credAvail = parseInt($("#credAvail").text());
		if(credSum + cred > credAvail){
			return true;
		}
		return false;
	}
	
	// 시간 겹치는지 확인
	function fn_isDuplTime(timeStr) {
		if(timeStr == null){
			return false; // null이 들어간 임시 데이터가 있음
		}
		
		timeArr = timeStr.split(', '); 

		for(idx in timeArr){
			if(regLectTimeStr.includes(timeArr[idx] + ', ')){ // 한번이라도 겹친다면 return true;
				return true;
			}
		}
		return false;
	}
	
	// 수강 신청 제거
	function fn_regLectDelete(lectOpenNum, lectName) {
	
		if(!confirm("[" + lectName + "] 강의를 수강 취소하시겠습니까?")){
			return;
		}
	
		$("#lectOpenNum").val(lectOpenNum);
		$("#frm").prop("action", "/student/registrationLecture/registrationLectureDelete");
		$("#frm").submit();
	}
	
	