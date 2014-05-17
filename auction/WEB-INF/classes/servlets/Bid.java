import java.sql.*;
import java.util.*;
import java.io.*;
import javax.servlet.http.*;
import javax.servlet.*;
 import javax.mail.*;
 import javax.mail.internet.*;
 import javax.activation.*;
import javax.servlet.http.*;
import javax.net.ssl.*;

public class Bid extends HttpServlet
{

	 Connection con=null;
   PreparedStatement ps=null;
   ResultSet rs1=null;
   ResultSet rs2=null;
 public void doGet(HttpServletRequest request,HttpServletResponse response)throws  IOException,ServletException
 {
	 try
  {
	 	 HttpSession session = request.getSession();
 if (session == null || session.getAttribute("Username") == null)
	 {
        // Forward the control to login.jsp if authentication fails or session expires
        request.getRequestDispatcher("/login.jsp").forward(request,
            response);
    }
   String auction_uuid=request.getParameter("auction_uuid");
   int bidder_id=Integer.parseInt(request.getParameter("bidder_userId"));
   int bid_amt=Integer.parseInt(request.getParameter("bid_amt"));
   String bidder_email=(String)request.getSession(false).getAttribute("emailId");
	String bid_uuid=UUID.randomUUID().toString().replaceAll("-", "");
	 String product_info=request.getParameter("product_info");
	 String author_email=request.getParameter("author_email");
	 List<String>mail_list = new ArrayList<String>();
	 String query;
	 boolean is_new_bidder=true;
	 int tot_bids=0;
	
if(author_email.equals(bidder_email))
	  {
	request.setAttribute("Bid_error_msg1", "You Cannot Bid Your Own Product!");
             RequestDispatcher rd= getServletContext().getRequestDispatcher("/viewad.jsp");
             rd.forward(request, response);
	  }
   else
	  {
	 Class.forName("com.mysql.jdbc.Driver");
     con = DriverManager.getConnection("jdbc:mysql://localhost:3306/bideasy","root", "root");
	 query="select no_of_bids from auction where auction_uuid=?";
	 ps= con.prepareStatement(query);
	 ps.setString(1,auction_uuid );
	 rs1=ps.executeQuery();
	 if(rs1!=null && rs1.next() )
	  {
     tot_bids=Integer.parseInt(rs1.getString("no_of_bids"));
	  }


      query="update auction set auction_bidder_id=?, no_of_bids=?,current_bid_amt=?,auction_start_price=? where auction_uuid=?";
          ps= con.prepareStatement(query);
		  ps.setInt(1,bidder_id);
		  ps.setInt(2,tot_bids+1);
		  ps.setInt(3,bid_amt);
		  ps.setInt(4,bid_amt);
		  ps.setString(5,auction_uuid);
	    
		 int i = ps.executeUpdate();

   query="select distinct auction_mail_list from bid where auction_uuid=?";
	 ps= con.prepareStatement(query);
	 ps.setString(1,auction_uuid );
	 rs2=ps.executeQuery();
      
	  while(rs2!=null && rs2.next())
	  {
      mail_list.add(rs2.getString("auction_mail_list"));
	  }
	  for (String eachmail : mail_list )
	  {
        if (eachmail.contains(bidder_email)) 
			{
			is_new_bidder=false;
		      }
			  else
		  {
				  is_new_bidder=true;
		  }
	  }
	  String recipients[]=mail_list.toArray(new String[mail_list.size()]);
    int j=0;
if(is_new_bidder)
	  {
	j =insert_record_in_bid_table(bid_uuid,bidder_email,auction_uuid);	
	  }
  else
	  {
      query="delete from bid where auction_mail_list=?";
	   ps= con.prepareStatement(query);
	     ps.setString(1,bidder_email);
		 int k=ps.executeUpdate();
		 if(k>0)
          j= insert_record_in_bid_table(bid_uuid,bidder_email,auction_uuid);	
		  
	  }
	if(i>0 && j>0)
	  {
		 
		 try
		  {
	new MailSend().SendMail(author_email,"Bid Offers to Your Product","A user have shown Interest on your Product-"+product_info+"\n your Product's Current Bid Amount is"+bid_amt+"\n Bidder Email id is"+ bidder_email);
	new MailSend().SendMail(bidder_email,"Thanks for Bidding! ","You Have successfully bid upon Product "+product_info+" \n  your bid id is "+bid_uuid+"\n Please stay tuned for updates.");
    new MailSend().SendBulkMail(recipients,"Auction Notification","Most Recent Bid on product "+product_info+" is at price "+bid_amt+"\n Bid more n more to Win! \n Please stay tuned for updates.");
		  }
		  catch (Exception e){}
		  try{
			  ServletContext context= getServletContext();
		  request.setAttribute("Bid_msg", "Successful Bid!");
		  RequestDispatcher rd= context.getRequestDispatcher("/viewad.jsp");
             rd.forward(request, response);
		 }
		 catch(Exception e1){}
	  }
	  else
	  {
    try{
		   ServletContext context= getServletContext();
		    request.setAttribute("Bid_error_msg", "Bid UnSuccessful! Try again.");
             RequestDispatcher rd= context.getRequestDispatcher("/viewad.jsp");
             rd.forward(request, response);
		  }
		  catch(Exception e){}
	  }
	  }
  }
  catch (SQLException e)
  {
	  e.printStackTrace();
	   try{
		   ServletContext context= getServletContext();
		    request.setAttribute("Bid_error_msg", "Bid UnSuccessful! Try again.");
             RequestDispatcher rd= context.getRequestDispatcher("/viewad.jsp");
             rd.forward(request, response);
		  }
		  catch(Exception e1){}
  }
  catch (ClassNotFoundException e)
  {
	  System.out.println("ClassNotFound exception");
	  e.printStackTrace();
  }
  finally
	{
	  if (ps != null) {
        try {
            ps.close();
        } catch (SQLException e) { /* ignored */}
                     }
					
					 if (rs1 != null) {
        try {
            rs1.close();
        } catch (SQLException e) { /* ignored */}
                     }
					 if (rs2 != null) {
        try {
            rs2.close();
        } catch (SQLException e) { /* ignored */}
                     }
    if (con != null) {
        try {
            con.close();
        } catch (SQLException e) { /* ignored */}
                          }
	}
   }
    public void doPost(HttpServletRequest request,HttpServletResponse response)throws  IOException,ServletException
 {
		doGet(request,response);
 }
 public int insert_record_in_bid_table(String bid_id,String bider_email,String auction_uid)
	{
	 
	String query="insert into bid (bidid,auction_mail_list,auction_uuid) values (?,?,?)";
try{
   	      ps= con.prepareStatement(query);
		  ps.setString(1,bid_id);
		  ps.setString(2,bider_email);
		  ps.setString(3,auction_uid);
	       int res=ps.executeUpdate();
		    return res;		  
	}
		   catch (SQLException e)
   {
	  e.printStackTrace();
	  return 0;
	}
}
}