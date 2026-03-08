package com.carwash.bookingservice.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateBookingRequest {
    
    /**
     * New booking date (Format: YYYY-MM-DD)
     */
    private String bookingDate;
    
    /**
     * New time slot (Format: HH:MM-HH:MM)
     */
    private String timeSlot;
    
    /**
     * Action type - set to "RESCHEDULE" to trigger rescheduling logic with limit tracking
     * Leave null or use "UPDATE" for simple date/time changes without tracking
     */
    private String action;
    
    /**
     * Optional reason for rescheduling (only used when action="RESCHEDULE")
     */
    private String rescheduledReason;
}


//package com.carwash.bookingservice.dto;
//import lombok.Data;
//import lombok.AllArgsConstructor;
//import lombok.NoArgsConstructor;
//
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//public class UpdateBookingRequest {
//	 private String bookingDate;  // Format: YYYY-MM-DD
//	 private String timeSlot;      // Format: HH:MM-HH:MM
//	 
//}
//
