/*
 * Heart Rate and SpO2 Algorithm - Based on Maxim Integrated Algorithm
 * Adapted from SparkFun Example8_SPO2.ino
 * https://github.com/sparkfun/SparkFun_MAX3010x_Sensor_Library/blob/master/examples/Example8_SPO2/
 * 
 * Algorithm originally by Maxim Integrated for MAX30102
 */

#ifndef MAXIM_ALGORITHM_H
#define MAXIM_ALGORITHM_H

#include <Arduino.h>

// Algorithm parameters
#define MAX30102_SAMPLE_LEN  100  // Buffer length (4 seconds at 25Hz)
#define FS                   25   // Sampling frequency (Hz)
#define BUFFER_SIZE          (FS * 4)
#define MA4_SIZE             4    // Moving average size
#define HAMMING_SIZE         5    // Hamming window size
#define MIN_HR               30   // Minimum heart rate
#define MAX_HR               220  // Maximum heart rate

// Function prototypes
void maxim_heart_rate_and_oxygen_saturation(
    uint32_t *pun_ir_buffer, 
    int32_t n_ir_buffer_length, 
    uint32_t *pun_red_buffer, 
    int32_t *pn_spo2, 
    int8_t *pch_spo2_valid, 
    int32_t *pn_heart_rate, 
    int8_t *pch_hr_valid
);

void maxim_find_peaks(
    int32_t *pn_locs, 
    int32_t *n_npks, 
    int32_t *pn_x, 
    int32_t n_size, 
    int32_t n_min_height, 
    int32_t n_min_distance, 
    int32_t n_max_num
);

void maxim_peaks_above_min_height(
    int32_t *pn_locs, 
    int32_t *n_npks, 
    int32_t *pn_x, 
    int32_t n_size, 
    int32_t n_min_height
);

void maxim_remove_close_peaks(
    int32_t *pn_locs, 
    int32_t *pn_npks, 
    int32_t *pn_x, 
    int32_t n_min_distance
);

void maxim_sort_ascend(int32_t *pn_x, int32_t n_size);
void maxim_sort_indices_descend(int32_t *pn_x, int32_t *pn_indx, int32_t n_size);

// Implementation

void maxim_heart_rate_and_oxygen_saturation(
    uint32_t *pun_ir_buffer, 
    int32_t n_ir_buffer_length, 
    uint32_t *pun_red_buffer, 
    int32_t *pn_spo2, 
    int8_t *pch_spo2_valid, 
    int32_t *pn_heart_rate, 
    int8_t *pch_hr_valid
) {
    uint32_t un_ir_mean;
    int32_t k, n_i_ratio_count = 0;
    int32_t i, n_exact_ir_valley_locs_count, n_middle_idx;
    int32_t n_th1, n_npks;   
    int32_t an_ir_valley_locs[15];
    int32_t n_peak_interval_sum;
    
    int32_t n_y_ac, n_x_ac;
    int32_t n_spo2_calc; 
    int32_t n_y_dc_max, n_x_dc_max; 
    int32_t n_y_dc_max_idx, n_x_dc_max_idx; 
    int32_t an_ratio[5], n_ratio_average; 
    int32_t n_nume, n_denom;

    // Calculate DC mean
    un_ir_mean = 0; 
    for (k = 0; k < n_ir_buffer_length; k++) {
        un_ir_mean += pun_ir_buffer[k];
    }
    un_ir_mean = un_ir_mean / n_ir_buffer_length;
    
    // Remove DC from IR signal  
    int32_t an_x[BUFFER_SIZE];
    for (k = 0; k < n_ir_buffer_length; k++) {
        an_x[k] = pun_ir_buffer[k] - un_ir_mean;
    }
    
    // 4-point moving average
    int32_t an_x_ma4[BUFFER_SIZE];
    for (k = 0; k < n_ir_buffer_length - MA4_SIZE; k++) {
        an_x_ma4[k] = (an_x[k] + an_x[k+1] + an_x[k+2] + an_x[k+3]) / 4;
    }
    
    // Calculate threshold  
    n_th1 = 0;
    for (k = 0; k < BUFFER_SIZE - MA4_SIZE; k++) {
        if (an_x_ma4[k] > n_th1) {
            n_th1 = an_x_ma4[k];
        }
    }
    n_th1 = n_th1 / 4;
    
    // Find peaks
    maxim_find_peaks(an_ir_valley_locs, &n_npks, an_x_ma4, BUFFER_SIZE - MA4_SIZE, n_th1, 4, 15);
    n_peak_interval_sum = 0;
    
    if (n_npks >= 2) {
        for (k = 1; k < n_npks; k++) {
            n_peak_interval_sum += (an_ir_valley_locs[k] - an_ir_valley_locs[k-1]);
        }
        n_peak_interval_sum = n_peak_interval_sum / (n_npks - 1);
        *pn_heart_rate = (int32_t)((FS * 60) / n_peak_interval_sum);
        *pch_hr_valid = 1;
    } else {
        *pn_heart_rate = -999;
        *pch_hr_valid = 0;
    }

    // Calculate SpO2 — remove DC from Red signal using Red mean
    uint32_t un_red_mean = 0;
    for (k = 0; k < n_ir_buffer_length; k++) {
        un_red_mean += pun_red_buffer[k];
    }
    un_red_mean = un_red_mean / n_ir_buffer_length;

    for (k = 0; k < n_ir_buffer_length; k++) {
        an_x[k] = pun_red_buffer[k] - un_red_mean;
    }

    // Find DC maximum
    n_y_dc_max = -16777216;
    for (k = 0; k < n_ir_buffer_length; k++) {
        if (an_x[k] > n_y_dc_max) {
            n_y_dc_max = an_x[k];
            n_y_dc_max_idx = k;
        }
    }
    
    n_x_dc_max = -16777216;
    for (k = 0; k < n_ir_buffer_length; k++) {
        if (pun_ir_buffer[k] > n_x_dc_max) {
            n_x_dc_max = pun_ir_buffer[k];
            n_x_dc_max_idx = k;
        }
    }
    
    // Calculate R value
    n_y_ac = (an_x[n_y_dc_max_idx] - an_x[n_y_dc_max_idx + 1]);
    n_x_ac = (an_x_ma4[n_x_dc_max_idx] - an_x_ma4[n_x_dc_max_idx + 1]);
    
    n_nume = (n_y_ac * n_x_dc_max) >> 7;
    n_denom = (n_x_ac * n_y_dc_max) >> 7;
    
    if (n_denom > 0 && n_i_ratio_count < 5 && n_nume != 0) {
        an_ratio[n_i_ratio_count] = (n_nume * 100) / n_denom;
        n_i_ratio_count++;
    }

    // Calculate SpO2 from R
    maxim_sort_ascend(an_ratio, n_i_ratio_count);
    n_middle_idx = n_i_ratio_count / 2;

    if (n_middle_idx > 1) {
        n_ratio_average = (an_ratio[n_middle_idx-1] + an_ratio[n_middle_idx]) / 2;
    } else {
        n_ratio_average = an_ratio[n_middle_idx];
    }

    if (n_ratio_average > 2 && n_ratio_average < 184) {
        n_spo2_calc = 110 - ((n_ratio_average - 2) / 2);
        *pn_spo2 = n_spo2_calc;
        *pch_spo2_valid = 1;
    } else {
        *pn_spo2 = -999;
        *pch_spo2_valid = 0;
    }
}

void maxim_find_peaks(
    int32_t *pn_locs, 
    int32_t *n_npks, 
    int32_t *pn_x, 
    int32_t n_size, 
    int32_t n_min_height, 
    int32_t n_min_distance, 
    int32_t n_max_num
) {
    maxim_peaks_above_min_height(pn_locs, n_npks, pn_x, n_size, n_min_height);
    maxim_remove_close_peaks(pn_locs, n_npks, pn_x, n_min_distance);
    *n_npks = min(*n_npks, n_max_num);
}

void maxim_peaks_above_min_height(
    int32_t *pn_locs, 
    int32_t *n_npks, 
    int32_t *pn_x, 
    int32_t n_size, 
    int32_t n_min_height
) {
    int32_t i = 1, n_width;
    *n_npks = 0;
    
    while (i < n_size - 1) {
        if (pn_x[i] > n_min_height && pn_x[i] > pn_x[i-1]) {
            n_width = 1;
            while (i + n_width < n_size && pn_x[i] == pn_x[i+n_width]) {
                n_width++;
            }
            if (pn_x[i] > pn_x[i+n_width] && (*n_npks) < 15) {
                pn_locs[(*n_npks)++] = i;
                i += n_width + 1;
            } else {
                i += n_width;
            }
        } else {
            i++;
        }
    }
}

void maxim_remove_close_peaks(
    int32_t *pn_locs, 
    int32_t *pn_npks, 
    int32_t *pn_x, 
    int32_t n_min_distance
) {
    int32_t i, j, n_old_npks, n_dist;
    
    maxim_sort_indices_descend(pn_x, pn_locs, *pn_npks);

    for (i = -1; i < *pn_npks; i++) {
        n_old_npks = *pn_npks;
        *pn_npks = i + 1;
        for (j = i + 1; j < n_old_npks; j++) {
            n_dist = pn_locs[j] - (i == -1 ? -1 : pn_locs[i]);
            if (n_dist > n_min_distance || n_dist < -n_min_distance) {
                pn_locs[(*pn_npks)++] = pn_locs[j];
            }
        }
    }
    maxim_sort_ascend(pn_locs, *pn_npks);
}

void maxim_sort_ascend(int32_t *pn_x, int32_t n_size) {
    int32_t i, j, n_temp;
    for (i = 1; i < n_size; i++) {
        n_temp = pn_x[i];
        for (j = i; j > 0 && n_temp < pn_x[j-1]; j--) {
            pn_x[j] = pn_x[j-1];
        }
        pn_x[j] = n_temp;
    }
}

void maxim_sort_indices_descend(int32_t *pn_x, int32_t *pn_indx, int32_t n_size) {
    int32_t i, j, n_temp;
    for (i = 1; i < n_size; i++) {
        n_temp = pn_indx[i];
        for (j = i; j > 0 && pn_x[n_temp] > pn_x[pn_indx[j-1]]; j--) {
            pn_indx[j] = pn_indx[j-1];
        }
        pn_indx[j] = n_temp;
    }
}

#endif
