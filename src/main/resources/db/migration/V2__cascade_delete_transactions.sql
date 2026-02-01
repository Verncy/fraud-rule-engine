-- Make fraud_cases follow its transaction
-- Deleting a transaction deletes the fraud_case (and rule_hits already cascades from fraud_cases)

ALTER TABLE fraud_cases
  DROP CONSTRAINT IF EXISTS fraud_cases_transaction_id_fkey;

ALTER TABLE fraud_cases
  ADD CONSTRAINT fraud_cases_transaction_id_fkey
  FOREIGN KEY (transaction_id)
  REFERENCES transactions(transaction_id)
  ON DELETE CASCADE;
