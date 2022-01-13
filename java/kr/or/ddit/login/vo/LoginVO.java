package kr.or.ddit.login.vo;

public class LoginVO {

   private String id;
   private String pw;
   private String accessRight;
   
   public String getId() {
      return id;
   }
   public void setId(String id) {
      this.id = id;
   }
   public String getPw() {
      return pw;
   }
   public void setPw(String pw) {
      this.pw = pw;
   }
   public String getAccessRight() {
      return accessRight;
   }
   public void setAccessRight(String accessRight) {
      this.accessRight = accessRight;
   }
   
   @Override
   public String toString() {
      return "LoginVO [id=" + id + ", pw=" + pw + ", accessRight=" + accessRight + "]";
   }
}