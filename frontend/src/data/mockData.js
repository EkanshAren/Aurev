
export const mockData = {
  notice: {
    type: "ASMT-10",
    noticeId: "GST/2026/001",
    din: "202608GST0001234567",
    gstin: "29AABCU9603R1ZM"
  },

  kpis: {
    demandedItc: 145000,
    explainedMismatch: 105000,
    netGap: 40000
  },

  reconciliation: [
    {
      supplier: "ABC Components Pvt Ltd",
      invoice: "ABC/2026/001",
      books: 45000,
      gstr2b: 45000,
      variance: 0,
      status: "MATCHED"
    },
    {
      supplier: "Delta Industrial Supplies",
      invoice: "DIS/2026/014",
      books: 55000,
      gstr2b: 25000,
      variance: 30000,
      status: "MISMATCH"
    },
    {
      supplier: "Eastern Trade Solutions",
      invoice: "ETS/2026/027",
      books: 25000,
      gstr2b: 25000,
      variance: 0,
      status: "MATCHED"
    },
    {
      supplier: "FineLine Traders",
      invoice: "FLT/2026/039",
      books: 20000,
      gstr2b: 10000,
      variance: 10000,
      status: "PARTIAL"
    }
  ],

  draft: "Respected Sir/Madam,\n\nWith reference to the GST notice, we respectfully submit our reconciliation and clarification for the ITC differences identified.",

  citations: [
    {
      title: "CBIC Circular No. 183/15/2022-GST",
      description: "Clarification regarding ITC differences."
    }
  ],

  audit: [
    {
      field: "GSTIN",
      original: "29AABCU9603R1ZM",
      masked: "29A*****603R1ZM"
    }
  ]
};
