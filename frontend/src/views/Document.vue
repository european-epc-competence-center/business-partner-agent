<!--
 Copyright (c) 2020 - for information on the respective copyright owner
 see the NOTICE file and/or the repository at
 https://github.com/hyperledger-labs/organizational-agent

 SPDX-License-Identifier: Apache-2.0
-->
<template>
  <v-container>
    <v-card v-if="isReady" class="mx-auto">
      <v-card-title class="bg-light">
        <v-btn
          depressed
          color="secondary"
          icon
          @click="$router.push({ name: 'Wallet' })"
        >
          <v-icon dark>$vuetify.icons.prev</v-icon>
        </v-btn>
        {{ schemaLabel }}
        <v-layout align-end justify-end>
          <v-btn
            v-if="this.id"
            depressed
            color="red"
            icon
            @click="deleteDocument()"
          >
            <v-icon dark>$vuetify.icons.delete</v-icon>
          </v-btn>
        </v-layout>
      </v-card-title>
      <v-card-text>
        <OrganizationalProfile
          v-if="isProfile(intDoc.type)"
          v-bind:documentData="document.documentData"
          ref="doc"
        ></OrganizationalProfile>
        <Credential
          v-else
          v-bind:document="document"
          ref="doc"
          @doc-field-changed="fieldModified"
          @doc-data-field-changed="documentDataFieldChanged"
        ></Credential>
        <v-divider></v-divider>
        <v-list-item>
          <v-list-item-content>
            <v-list-item-title>Public Profile</v-list-item-title>
            <v-list-item-subtitle
              >Visible in Public Profile</v-list-item-subtitle
            >
          </v-list-item-content>
          <v-list-item-action>
            <v-switch
              :disabled="document.type === CredentialTypes.UNKNOWN.type"
              v-model="document.isPublic"
              @change="fieldModified()"
            ></v-switch>
          </v-list-item-action>
        </v-list-item>
        <v-divider></v-divider>

        <v-list-item
          v-if="this.id && !isProfile(document.type)"
          :disabled="docModified()"
        >
          <v-tooltip right v-model="showTooltip">
            <template v-slot:activator="{ attrs }">
              <v-list-item-content>
                <v-list-item-title>Verification</v-list-item-title>
                <v-list-item-subtitle
                  >Request a verification</v-list-item-subtitle
                >
              </v-list-item-content>

              <v-list-item-action>
                <!-- TODO übergabe der parameter mit schemaId params {type: { type: meinType schemaId: schemaId }}-->
                <v-btn
                  v-bind="attrs"
                  icon
                  :to="{
                    name: 'RequestVerification',
                    params: { documentId: id, schemaId: intDoc.schemaId },
                  }"
                  :disabled="docModified()"
                >
                  <v-icon color="grey">$vuetify.icons.next</v-icon>
                </v-btn>
              </v-list-item-action>
            </template>
            <span
              >Document modified, please save before start verification</span
            >
          </v-tooltip>
        </v-list-item>

        <v-divider></v-divider>
      </v-card-text>

      <v-card-actions>
        <v-layout align-end justify-end>
          <v-bpa-button color="secondary" @click="cancel()">Cancel</v-bpa-button>
          <v-bpa-button
            :loading="this.isBusy"
            color="primary"
            @click="saveDocument(false || isProfile(intDoc.type))"
            >Save</v-bpa-button
          >
          <v-bpa-button
            v-show="this.id && !isProfile(intDoc.type)"
            :loading="this.isBusy"
            color="primary"
            @click="saveDocument(true && !isProfile(intDoc.type))"
            >Save & Close</v-bpa-button
          >
        </v-layout>
      </v-card-actions>

      <v-expansion-panels v-if="expertMode" accordion flat>
        <v-expansion-panel>
          <v-expansion-panel-header
            class="grey--text text--darken-2 font-weight-medium bg-light"
            >Show raw data</v-expansion-panel-header
          >
          <v-expansion-panel-content class="bg-light">
            <vue-json-pretty :data="document"></vue-json-pretty>
          </v-expansion-panel-content>
        </v-expansion-panel>
      </v-expansion-panels>
    </v-card>
  </v-container>
</template>

<script>
import { EventBus } from "../main";
import { CredentialTypes } from "../constants";
import OrganizationalProfile from "@/components/OrganizationalProfile";
import Credential from "@/components/Credential";
import VBpaButton from "@/components/BpaButton";

export default {
  name: "Document",
  props: {
    id: {
      type: String,
      required: false,
    },
    type: {
      type: String,
      required: false,
    },
    schemaId: {
      type: String,
      required: false,
    },
  },
  created() {
    if (this.id) {
      EventBus.$emit("title", "Edit Document");
      this.getDocument();
    } else if (!this.type) {
      this.$router.push({ name: "Wallet" });
    } else {
      EventBus.$emit("title", "Create new Document");
      this.document.type = this.type;
      this.document.schemaId = this.schemaId;
      this.document.isPublic = this.isProfile(this.document.type);
      this.isReady = true;
      this.intDoc = { ...this.document };
    }
  },
  data: () => {
    return {
      document: {},
      intDoc: {},
      isBusy: false,
      isReady: false,
      CredentialTypes,
      docChanged: false,
      credChanged: false,
      showTooltip: false,
    };
  },
  computed: {
    expertMode() {
      return this.$store.state.expertMode;
    },
    schemaLabel() {
      if (this.schemaId) {
        return this.$store.getters.getSchemaById(this.schemaId).label;
      } else if (this.type) {
        return this.$store.getters.getSchemaByType(this.type).label;
      } else {
        return null;
      }
    },
  },
  watch: {},
  methods: {
    getDocument() {
      this.$axios
        .get(`${this.$apiBaseUrl}/wallet/document/${this.id}`)
        .then((result) => {
          if ({}.hasOwnProperty.call(result, "data")) {
            this.document = result.data;
            this.intDoc = { ...this.document };
            this.isReady = true;
          }
        })
        .catch((e) => {
          console.error(e);
          EventBus.$emit("error", e);
        });
    },
    saveDocument(closeDocument) {
      this.isBusy = true;
      if (this.id) {
        this.$axios
          .put(`${this.$apiBaseUrl}/wallet/document/${this.id}`, {
            document: this.document.documentData,
            isPublic: this.document.isPublic,
            label: this.isProfile(this.document.type)
              ? this.document.documentData.legalName
              : this.document.label,
            type: this.document.type,
            schemaId: this.document.schemaId,
          })
          .then((res) => {
            console.log(res);
            this.isBusy = false;
            if (closeDocument) {
              this.$router.push({
                name: "Wallet",
              });
            } else {
              this.$router.go(this.$router.currentRoute);
            }
            EventBus.$emit("success", "Success");
          })
          .catch((e) => {
            this.isBusy = false;
            console.error(e);
            EventBus.$emit("error", e);
          });

        // create new document
      } else {
        const docForSave = {
          document: this.$refs.doc.intDoc.documentData,
          label: this.$refs.doc.intDoc.label,
          isPublic: this.document.isPublic,
          type: this.type,
          schemaId: this.schemaId,
        };
        this.$axios
          .post(`${this.$apiBaseUrl}/wallet/document`, docForSave)
          .then((res) => {
            console.log(res);
            this.isBusy = false;
            this.$router.push({
              name: "Wallet",
            });
            EventBus.$emit("success", "Success");
          })
          .catch((e) => {
            this.isBusy = false;
            console.error(e);
            EventBus.$emit("error", e);
          });
      }
    },
    deleteDocument() {
      this.$axios
        .delete(`${this.$apiBaseUrl}/wallet/document/${this.id}`)
        .then((result) => {
          if (result.status === 200) {
            EventBus.$emit("success", "Document deleted");
            this.$router.push({
              name: "Wallet",
            });
          }
        })
        .catch((e) => {
          console.error(e);
          EventBus.$emit("error", e);
        });
    },
    cancel() {
      this.$router.push({
        name: "Wallet",
      });
    },
    isProfile(schemaType) {
      return !this.schemaId && schemaType === CredentialTypes.PROFILE.type;
    },
    fieldModified() {
      const isModified = Object.keys(this.intDoc).find((key) => {
        return this.document[key] != this.intDoc[key];
      })
        ? true
        : false;
      this.docChanged = isModified;
      this.docModified();
    },
    documentDataFieldChanged(credChanged) {
      this.credChanged = credChanged;
      this.docModified();
    },
    docModified() {
      this.showTooltip = this.docChanged || this.credChanged;
      return this.docChanged || this.credChanged;
    },
  },
  components: {
    VBpaButton,
    OrganizationalProfile,
    Credential,
  },
};
</script>

<style scoped>
.bg-light {
  background-color: #fafafa;
}

.bg-light-2 {
  background-color: #ececec;
}
</style>
