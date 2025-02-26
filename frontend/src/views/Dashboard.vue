<!--
 Copyright (c) 2020 - for information on the respective copyright owner
 see the NOTICE file and/or the repository at
 https://github.com/hyperledger-labs/organizational-agent

 SPDX-License-Identifier: Apache-2.0
-->
<template>
  <v-container text-center>
    <v-card flat v v-if="!isLoading" class="mx-auto">
      <v-row>
        <v-col class="col-sm-10 offset-sm-1 col-md-8 offset-md-2">
          <v-text-field
            id="did"
            v-model="status.did"
            readonly
            outlined
            dense
            label="DID"
            :append-icon="'$vuetify.icons.copy'"
            @click:append="copyDid"
          ></v-text-field>
        </v-col>
      </v-row>
    </v-card>
    <div v-if="isWelcome && !isLoading">
      <!-- Image from undraw.co -->
      <v-img
        class="mx-auto"
        src="@/assets/undraw_welcome_3gvl_grey.png"
        max-width="300"
        aspect-ratio="1"
      ></v-img>
      <p
        v-bind:style="{ fontSize: `180%` }"
        class="grey--text text--darken-2 font-weight-medium"
      >
        Hi, we've already set up an identity for you!
      </p>
      <!-- <p v-bind:style="{ fontSize: `140%` }" class="grey--text text--darken-2 font-weight-medium">Start by adding a public profile that your business partners will see</p> -->
      <br />
      <v-bpa-button
        color="primary"
        :to="{
          name: 'DocumentAdd',
          params: { type: CredentialTypes.PROFILE.type },
        }"
        >Setup your Profile</v-bpa-button
      >
    </div>
    <div v-if="!isWelcome && !isLoading">
      <v-row>
        <v-col class="col-sm-5 offset-sm-1 col-md-4 offset-md-2">
          <v-card class="mx-auto" :to="{ name: 'Wallet' }">
            <v-img
              class="align-end"
              src="@/assets/undraw_certification_aif8.png"
            ></v-img>
            <v-card-title class="justify-center">
              <span class="cardTitle">
                {{ status.credentials }}
              </span>
              <span class="newHighlight" v-show="newCredentialsCount > 0">
                (+{{ newCredentialsCount }})
              </span>
            </v-card-title>
            <v-card-title class="justify-center"
              >Verified Credentials</v-card-title
            >
          </v-card>
        </v-col>
        <v-col class="col-sm-5 col-md-4">
          <v-card class="mx-auto" :to="{ name: 'Partners' }">
            <!-- FIXME Used aspect ratio as a hacky way to make the cards the same height -->
            <v-img
              class="align-end"
              aspect-ratio="1.29"
              src="@/assets/undraw_agreement_aajr.png"
            ></v-img>
            <v-card-title class="justify-center">
              <span class="cardTitle">
                {{ status.partners }}
              </span>
              <span class="newHighlight" v-show="newPartnersCount > 0">
                (+{{ newPartnersCount }})
              </span>
              <span class="newHighlight" v-show="newPartnerEventsCount > 0">
                (+{{ newPartnerEventsCount }})
              </span>
            </v-card-title>
            <v-card-title class="justify-center"
              >Business Partners</v-card-title
            >
          </v-card>
        </v-col>
      </v-row>
    </div>
  </v-container>
</template>

<script>
import { EventBus } from "../main";
import { CredentialTypes } from "../constants";
import VBpaButton from "@/components/BpaButton";
export default {
  name: "Dashboard",
  components: {VBpaButton},
  created() {
    EventBus.$emit("title", "Dashboard");
    this.getStatus();
  },
  data: () => {
    return {
      isWelcome: true,
      isLoading: true,
      CredentialTypes,
    };
  },
  computed: {
    newPartnerEventsCount() {
      return this.$store.getters.newPartnerEventsCount;
    },
    newPartnersCount() {
      return this.$store.getters.newPartnersCount;
    },
    newCredentialsCount() {
      return this.$store.getters.newCredentialsCount;
    },
  },
  methods: {
    getStatus() {
      console.log("Getting status...");
      this.$axios
        .get(`${this.$apiBaseUrl}/status`)
        .then((result) => {
          console.log(result);
          this.isWelcome = !result.data.profile;
          this.status = result.data;
          this.isLoading = false;
        })
        .catch((e) => {
          console.error(e);
          this.isLoading = false;
          EventBus.$emit("error", e);
        });
    },
    copyDid() {
      let didEl = document.querySelector("#did");
      didEl.select();
      let successfull;
      try {
        successfull = document.execCommand("copy");
      } catch (err) {
        successfull = false;
      }
      successfull
        ? EventBus.$emit("success", "DID copied")
        : EventBus.$emit("error", "Can't copy DID");
      didEl.blur();
      window.getSelection().removeAllRanges();
    },
  },
};
</script>
<style scoped>
.newHighlight {
  color: #2ecc71;
  padding-left: 4px;
  font-size: 200%;
}
.cardTitle {
  font-size: 400%;
  margin-bottom: 2;
}
</style>
