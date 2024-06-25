/* tslint:disable */
/* eslint-disable */
// Generated using typescript-generator version 3.2.1263 on 2024-06-25 16:48:20.

export interface SchwabCheckingData {
    deposits: SchwabCheckingGroups;
    creditCardWithdrawals: SchwabCheckingGroups;
    otherWithdrawals: SchwabCheckingGroups;
    firstRow: SchwabCheckingRow;
    startingBalance: number;
    lastRow: SchwabCheckingRow;
    sanityCheckLogs: string[];
    appleSavingsInterest: number;
}

export interface SchwabCheckingGroups {
    groups: { [index: string]: SchwabCheckingGroup };
    totals: { [index: string]: number };
    grandTotal: number;
}

export interface SchwabCheckingRow extends TrDateRow {
    trType: SchwabCheckingTransactionType;
    checkNum: number | null;
    desc: string;
    withdrawal: number | null;
    deposit: number | null;
    balance: number;
    withdrawalN: number;
    depositN: number;
}

export interface SchwabCheckingGroup {
    groupName: string;
    rows: SchwabCheckingRow[];
    extraInfoSubsumedGroups: SchwabCheckingGroup[] | null;
    total: number;
}

export interface TrDateRow {
    trDate: Date;
}

export type SchwabCheckingTransactionType = "ACH" | "WIRE" | "CHECK" | "CREDIT" | "DEPOSIT" | "INTADJUST" | "TRANSFER" | "VISA" | "FEE" | "RETURNITEM" | "ATM" | "ATMREBATE";
