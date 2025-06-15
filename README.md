# Virtual_Environments
Design and Configuration of Automated Production Systems using Virtual Environments

![Screenshot 2025-06-15 at 15 58 51](https://github.com/user-attachments/assets/5f41ccd0-a3e7-496f-9711-96426e30cbe4)


## Welcome to the project Virtual_Environments wiki!

_This repository contains the project carried out during the Master of Science in Business Engineering at SUPSI (University of Applied Sciences and Arts of Southern Switzerland) in Lugano, under the supervision of Prof. Diego Rovere. Using the DDD Model Editor simulator, InfluxDB and Grafana, we built a virtual-environment simulation of automated production systems with a dashboard to show the real time performances._

![Screenshot 2025-06-15 at 16 08 19](https://github.com/user-attachments/assets/f6c0d6c9-daae-436a-9d06-d2dd58dc9ccc)



## Process Description

1. **Part Production (Station S1)**  
   Produces two PCB board types:  
   - **A1**  
   - **A2**  
   Robots will later mount additional components onto these boards.

2. **Loading Empty Boards (Station K1 → K2 / K4)**  
   Robot **R3** picks up empty boards from `K1` and places each board into the next available shuttle position:  
   - `K2` (for R1)  
   - `K4` (for R2)  
   R3 may choose freely between K2 and K4.

3. **Component Assembly (Stations K3 & K5)**  
   - **R1** works at `K3`; **R2** works at `K5`.  
   - Refer to the PCB layout for exact component locations.  
   - **Component requirements:**  
     - **A1** boards require 4 × type C components and 2 × type D components  
     - **A2** boards require 4 × type C components and 4 × type D components

4. **Batch Palletizing (Station K6)**  
   Once assembly is complete, R3 retrieves the finished boards and places them on the batch pallet at `K6`.  
   - Only boards of the same type may share a pallet.  
   - Partially filled batches must be released before starting a new batch.

5. **Curing in Ovens (Stations K7–K9)**  
   - **F1** (two-position oven at `K7` & `K8`) begins heating only when both positions are occupied.  
   - **F2** (single-position oven at `K9`) processes one board at a time.  
   - Heating time varies by number of boards and differs between F1 and F2 (see oven-speed table).

6. **Quality Inspection (Station K10)**  
   Performs individual board checks.  
   - Defect rates: 1% for A1 boards; 2% for A2 boards.

7. **Defective-Board Removal (Station K11 → D2)**  
   Robot **R4** picks defective boards from `K10` and transports them to disposal station `D2`.  
********

## Process Map

<img width="808" alt="image" src="https://github.com/user-attachments/assets/e1512fd4-967a-49cf-8e32-80f1786673be" />

