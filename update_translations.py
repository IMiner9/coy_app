#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import csv
import sys

# 한국어 텍스트에 대한 영어 번역 매핑 (소스 코드에서 확인한 텍스트들)
translations = {
    "기념일": "Anniversary",
    "캘린더": "Calendar", 
    "기념일 추가": "Add Anniversary",
    "전체": "All",
    "생일": "Birthday",
    "데이트": "Date",
    "중요한 날": "Important Day",
    "지난 기념일": "Past Anniversaries",
    "기념일이 없습니다": "No anniversaries found",
    "플로팅 + 버튼을 눌러서 기념일을 추가하거나 기념일 데이터를 입력한 코드를 확인하세요.": "Press the floating + button to add an anniversary or check the code where anniversary data is entered.",
    "삭제": "Delete",
    "기념일 삭제": "Delete Anniversary",
    "정말 삭제하시겠습니까?": "Are you sure you want to delete?",
    "확인": "Confirm",
    "기념일 수정": "Edit Anniversary",
    "기념일 이름": "Anniversary Name",
    "설명": "Description",
    "시작일": "Start Date",
    "종료일": "End Date",
    "시간설정": "Set Time",
    "시간 없음": "No Time",
    "날짜를 선택해주세요.": "Please select a date.",
    "카테고리": "Category",
    "색상": "Color",
    "사진 (선택)": "Photo (Optional)",
    "저장": "Save",
    "날짜 선택": "Select Date",
    "이름을 입력해주세요": "Please enter your name",
    "별명 입력": "Enter Nickname",
    "사귀기 시작한 날": "Relationship Start Date",
    "연락처": "Phone Number",
    "MBTI": "MBTI",
    "좋아하는 것": "Favorites",
    "취미": "Hobbies",
    "현재 기분": "Current Mood",
    "연인에게 한 줄 메모": "Note to Partner",
    "메모 입력": "Enter Note",
    "사진 입력해주세요": "Please enter a photo",
    "편집하기": "Edit",
    "프로필 편집": "Edit Profile",
    "이름": "Name",
    "이름을 입력하세요": "Enter your name",
    "별명": "Nickname",
    "별명을 입력하세요": "Enter your nickname",
    "날짜를 선택하세요": "Select a date",
    "성별": "Gender",
    "취소": "Cancel",
    "함께한 추억": "Memories Together",
    "일기 추가": "Add Memory",
    "일기 삭제": "Delete Memory",
    "의 취향": "'s Preferences",
    "좋아하는 음식 모아보기": "View Favorite Foods",
    "커피, 차, 주스 등": "Coffee, tea, juice, etc.",
    "좋아하는 노래 기록": "Record Favorite Songs",
    "인생 영화 목록": "Life Movie List",
    "가고 싶은 곳 / 다녀온 곳": "Places to Visit / Places Visited",
    "받고 싶은 선물 정리": "Organize Gifts You Want to Receive",
    "함께 즐기는 활동들": "Activities Enjoyed Together",
    "좋아하는 대사나 문장": "Favorite Lines or Sentences",
    "닫기": "Close",
    "프로필": "Profile",
    "좋아하는것": "Favorites",
    "추억": "Memories",
    "애인의 프로필": "Partner Profile",
    "좋아하는 것들": "Favorites",
    "일정 캘린더": "Schedule Calendar",
    "연인 프로필 앱": "Couple Profile App",
    "내사랑": "My Love",
    "음식": "Food",
    "음료": "Drinks",
    "음악": "Music",
    "영화": "Movies",
    "여행": "Travel",
    "선물": "Gifts",
    "말 / 표현": "Words / Expressions",
    "년도와 월을 선택하세요": "Select Year and Month",
    "년도": "Year",
    "월": "Month",
    "1월": "January",
    "2월": "February",
    "3월": "March",
    "4월": "April",
    "5월": "May",
    "6월": "June",
    "7월": "July",
    "8월": "August",
    "9월": "September",
    "10월": "October",
    "11월": "November",
    "12월": "December",
    "이전 달": "Previous Month",
    "다음 달": "Next Month",
    "일": "Day",
    "년": "Year",
    "이 날짜에는 연결된 기념일이 없습니다.": "No connected anniversaries on this date.",
    "제목": "Title",
    "사진": "Photo",
    "사진 크기": "Photo Size",
    "추억 삭제": "Delete Memory",
    "추억 수정": "Edit Memory",
    "추억 작성": "Write Memory",
    "좋아하는 것 추가": "Add Favorite",
    "좋아하는 것 삭제": "Delete Favorite",
    "좋아하는 것 수정": "Edit Favorite",
    "카테고리 선택": "Select Category",
    "사진 추가하시려면 클릭하세요": "Click to add a photo",
    "사진 추가": "Add Photo",
    "사진 삭제": "Delete Photo",
    "사진 수정": "Edit Photo",
    "사진 변경": "Change Photo",
    "기본 아이콘": "Default Icon",
    "이벤트 카테고리": "Event Category",
    "앨범에서 선택": "Select from Album",
    "기본 프로필로 설정": "Set as Default Profile",
    "사진 설정": "Photo Settings",
    "프로필 사진 크롭": "Crop Profile Photo",
    "기본 프로필로 변경": "Change to Default Profile",
    "프로필 사진 크기": "Profile Photo Size",
    "미입력": "Not entered",
    "별명 미입력": "No nickname",
    "메모를 입력해주세요": "Please enter a memo",
}

def translate_text(korean_text):
    """한국어 텍스트를 영어로 번역"""
    if not korean_text or korean_text.strip() == "":
        return ""
    
    # 직접 매핑된 번역이 있으면 사용
    if korean_text in translations:
        return translations[korean_text]
    
    # 부분 매칭 시도
    for key, value in translations.items():
        if key in korean_text:
            return value
    
    return ""

def process_csv(input_file, output_file):
    """CSV 파일을 읽어서 영어 번역을 추가"""
    rows = []
    
    # UTF-8 BOM으로 파일 읽기
    with open(input_file, 'r', encoding='utf-8-sig') as f:
        reader = csv.reader(f)
        for i, row in enumerate(reader):
            if i == 0:
                # 헤더 행
                rows.append(row)
            elif i == 1:
                # 한글 헤더 행
                rows.append(row)
            else:
                # 데이터 행
                if len(row) >= 3:
                    code = row[0]
                    page = row[1]
                    korean_text = row[2].strip('"')  # 따옴표 제거
                    existing_english = row[3].strip() if len(row) > 3 else ""
                    
                    # 이미 영어 번역이 있으면 유지
                    if existing_english and existing_english.strip():
                        english = existing_english
                    else:
                        # 번역 생성
                        english = translate_text(korean_text)
                    
                    # CSV 행 재구성
                    new_row = [code, page]
                    # 쉼표나 따옴표가 포함된 텍스트는 따옴표로 감싸기
                    if ',' in korean_text or '\n' in korean_text or '"' in korean_text:
                        new_row.append('"' + korean_text.replace('"', '""') + '"')
                    else:
                        new_row.append(korean_text)
                    
                    new_row.append(english)
                    rows.append(new_row)
                else:
                    rows.append(row)
    
    # UTF-8 BOM으로 파일 쓰기
    with open(output_file, 'w', encoding='utf-8-sig', newline='') as f:
        writer = csv.writer(f)
        writer.writerows(rows)

if __name__ == "__main__":
    input_file = "app_translations.csv"
    output_file = "app_translations.csv"
    process_csv(input_file, output_file)
    print(f"English translations added to {output_file}")










