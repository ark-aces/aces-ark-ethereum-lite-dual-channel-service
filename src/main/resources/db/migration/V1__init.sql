CREATE SCHEMA ark_ethereum_channel;

CREATE TABLE ark_ethereum_channel.contracts (
  pid BIGSERIAL PRIMARY KEY,
  id VARCHAR(255) NOT NULL,
  correlation_id VARCHAR(255),
  status VARCHAR(20),
  recipient_eth_address VARCHAR(255),
  return_ark_address VARCHAR(255),
  deposit_ark_address VARCHAR(255),
  deposit_ark_address_passphrase VARCHAR(255),
  created_at TIMESTAMP
);
CREATE INDEX ON ark_ethereum_channel.contracts (id);
CREATE INDEX ON ark_ethereum_channel.contracts (correlation_id);
CREATE INDEX ON ark_ethereum_channel.contracts (status);
CREATE INDEX ON ark_ethereum_channel.contracts (recipient_eth_address);
CREATE INDEX ON ark_ethereum_channel.contracts (deposit_ark_address);
CREATE INDEX ON ark_ethereum_channel.contracts (return_ark_address);
CREATE INDEX ON ark_ethereum_channel.contracts (created_at);

CREATE TABLE ark_ethereum_channel.transfers (
  pid BIGSERIAL PRIMARY KEY,
  id VARCHAR(255) NOT NULL,
  created_at TIMESTAMP,
  contract_pid BIGINT NOT NULL,
  status VARCHAR(255),
  ark_transaction_id VARCHAR(255),
  ark_amount DECIMAL(40,8),
  ark_to_eth_rate DECIMAL(40,8),
  ark_flat_fee DECIMAL(40,8),
  ark_percent_fee DECIMAL(40,8),
  ark_total_fee DECIMAL(40,8),
  eth_send_amount DECIMAL(40,8),
  eth_transaction_id VARCHAR(255),
  needs_ark_return BOOLEAN,
  return_ark_transaction_id VARCHAR(255)
);
ALTER TABLE ark_ethereum_channel.transfers ADD FOREIGN KEY (contract_pid) REFERENCES ark_ethereum_channel.contracts (pid);

CREATE INDEX ON ark_ethereum_channel.transfers (id);
CREATE INDEX ON ark_ethereum_channel.transfers (created_at);
CREATE INDEX ON ark_ethereum_channel.transfers (contract_pid);
CREATE INDEX ON ark_ethereum_channel.transfers (status);
CREATE INDEX ON ark_ethereum_channel.transfers (ark_transaction_id);
CREATE INDEX ON ark_ethereum_channel.transfers (ark_amount);
CREATE INDEX ON ark_ethereum_channel.transfers (eth_transaction_id);
CREATE INDEX ON ark_ethereum_channel.transfers (return_ark_transaction_id);

CREATE TABLE ark_ethereum_channel.service_capacities (
  pid BIGSERIAL PRIMARY KEY,
  available_amount DECIMAL(40, 8),
  unsettled_amount DECIMAL(40, 8),
  total_amount DECIMAL(40, 8),
  unit VARCHAR(20),
  updated_at TIMESTAMP,
  created_at TIMESTAMP
);
CREATE INDEX ON ark_ethereum_channel.service_capacities (available_amount);
CREATE INDEX ON ark_ethereum_channel.service_capacities (unsettled_amount);
CREATE INDEX ON ark_ethereum_channel.service_capacities (total_amount);
CREATE INDEX ON ark_ethereum_channel.service_capacities (unit);
CREATE INDEX ON ark_ethereum_channel.service_capacities (updated_at);
CREATE INDEX ON ark_ethereum_channel.service_capacities (created_at);


CREATE SCHEMA ethereum_ark_channel;

CREATE TABLE ethereum_ark_channel.contracts (
  pid BIGSERIAL PRIMARY KEY,
  id VARCHAR(255) NOT NULL,
  correlation_id VARCHAR(255),
  status VARCHAR(20),
  recipient_ark_address VARCHAR(255),
  return_eth_address VARCHAR(255),
  deposit_eth_address VARCHAR(255),
  deposit_eth_address_passphrase VARCHAR(255),
  created_at TIMESTAMP
);
CREATE INDEX ON ethereum_ark_channel.contracts (id);
CREATE INDEX ON ethereum_ark_channel.contracts (correlation_id);
CREATE INDEX ON ethereum_ark_channel.contracts (status);
CREATE INDEX ON ethereum_ark_channel.contracts (recipient_ark_address);
CREATE INDEX ON ethereum_ark_channel.contracts (deposit_eth_address);
CREATE INDEX ON ethereum_ark_channel.contracts (return_eth_address);
CREATE INDEX ON ethereum_ark_channel.contracts (created_at);

CREATE TABLE ethereum_ark_channel.transfers (
  pid BIGSERIAL PRIMARY KEY,
  id VARCHAR(255) NOT NULL,
  created_at TIMESTAMP,
  contract_pid BIGINT NOT NULL,
  status VARCHAR(255),
  eth_transaction_id VARCHAR(255),
  eth_amount DECIMAL(40,8),
  eth_to_ark_rate DECIMAL(40,8),
  eth_flat_fee DECIMAL(40,8),
  eth_percent_fee DECIMAL(40,8),
  eth_total_fee DECIMAL(40,8),
  ark_send_amount DECIMAL(40,8),
  ark_transaction_id VARCHAR(255),
  needs_eth_return BOOLEAN,
  return_eth_transaction_id VARCHAR(255)
);
ALTER TABLE ethereum_ark_channel.transfers ADD FOREIGN KEY (contract_pid) REFERENCES ethereum_ark_channel.contracts (pid);

CREATE INDEX ON ethereum_ark_channel.transfers (id);
CREATE INDEX ON ethereum_ark_channel.transfers (created_at);
CREATE INDEX ON ethereum_ark_channel.transfers (contract_pid);
CREATE INDEX ON ethereum_ark_channel.transfers (status);
CREATE INDEX ON ethereum_ark_channel.transfers (eth_transaction_id);
CREATE INDEX ON ethereum_ark_channel.transfers (eth_amount);
CREATE INDEX ON ethereum_ark_channel.transfers (ark_transaction_id);
CREATE INDEX ON ethereum_ark_channel.transfers (return_eth_transaction_id);

CREATE TABLE ethereum_ark_channel.service_capacities (
  pid BIGSERIAL PRIMARY KEY,
  available_amount DECIMAL(40, 8),
  unsettled_amount DECIMAL(40, 8),
  total_amount DECIMAL(40, 8),
  unit VARCHAR(20),
  updated_at TIMESTAMP,
  created_at TIMESTAMP
);
CREATE INDEX ON ethereum_ark_channel.service_capacities (available_amount);
CREATE INDEX ON ethereum_ark_channel.service_capacities (unsettled_amount);
CREATE INDEX ON ethereum_ark_channel.service_capacities (total_amount);
CREATE INDEX ON ethereum_ark_channel.service_capacities (unit);
CREATE INDEX ON ethereum_ark_channel.service_capacities (updated_at);
CREATE INDEX ON ethereum_ark_channel.service_capacities (created_at);

